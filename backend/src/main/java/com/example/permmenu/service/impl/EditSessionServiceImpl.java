package com.example.permmenu.service.impl;

import com.example.permmenu.entity.EditLock;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.mapper.EditLockMapper;
import com.example.permmenu.mapper.PermMenuMapper;
import com.example.permmenu.service.EditSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 编辑会话服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditSessionServiceImpl implements EditSessionService {

    private final EditLockMapper editLockMapper;
    private final PermMenuMapper permMenuMapper;
    private final JdbcTemplate jdbcTemplate;

    // 内存中的 SQL 审计日志，key 为 tempTableName
    private final Map<String, List<String>> sqlLogs = new ConcurrentHashMap<>();

    @Override
    public EditLock getActiveLock() {
        List<EditLock> activeLocks = editLockMapper.selectActiveLocks(2);
        if (!activeLocks.isEmpty()) {
            return activeLocks.get(0);
        }
        return null;
    }

    /**
     * 校验输入是否只包含合法字符（字母、数字、下划线），防止 SQL 注入
     */
    private void validateIdentifier(String input, String fieldName) {
        if (input != null && !input.isEmpty() && !input.matches("^[A-Za-z0-9_]+$")) {
            throw new IllegalArgumentException(fieldName + " 包含非法字符，仅允许字母、数字和下划线");
        }
    }

    /**
     * 校验临时表名格式是否合法
     */
    private void validateTempTableName(String tempTableName) {
        if (tempTableName == null || !tempTableName.matches("^perm_menu_\\d+$")) {
            throw new IllegalArgumentException("临时表名格式非法，应为 perm_menu_ 加时间戳");
        }
    }

    /**
     * 校验当前锁是否被后续用户挤占（软保护超时后被他人解锁）
     */
    private void checkNotEvicted(String tempTableName) {
        EditLock lock = editLockMapper.selectByTempTable(tempTableName);
        if (lock != null) {
            int newerLocks = editLockMapper.countNewerLocks(lock.getId());
            if (newerLocks > 0) {
                dropTempTable(tempTableName);
                throw new RuntimeException("由于您操作时间过长，且期间已有新用户解锁过系统，您此次的编辑已无效被强制废弃，请重新刷新页面再试！");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unlockSession(String lockedBy, String subsystemCode) {
        // 0. 校验输入合法性，防止 SQL 注入
        validateIdentifier(subsystemCode, "子系统编码");

        // 1. 校验是否有活跃锁
        EditLock activeLock = getActiveLock();
        if (activeLock != null) {
            throw new RuntimeException("当前已有用户 " + activeLock.getLockedBy() + " 正在编辑中，开始时间：" + activeLock.getLockedAt());
        }

        // 2. 生成临时表名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String tempTableName = "perm_menu_" + timestamp;

        // 3. 创建临时表（支持全量或按子系统复制）
        String createTableSql;
        if (subsystemCode == null || subsystemCode.trim().isEmpty()) {
            createTableSql = "CREATE TABLE " + tempTableName + " AS SELECT * FROM perm_menu";
        } else {
            createTableSql = "CREATE TABLE " + tempTableName + " AS SELECT * FROM perm_menu WHERE SUBSYSTEM_CODE = '" + subsystemCode + "'";
        }
        jdbcTemplate.execute(createTableSql);

        // 4. 插入锁记录
        EditLock lock = new EditLock();
        lock.setLockedBy(lockedBy);
        lock.setTempTableName(tempTableName);
        lock.setSubsystemCode(subsystemCode);
        editLockMapper.insertLock(lock);

        // 5. 初始化 SQL 日志
        sqlLogs.put(tempTableName, new ArrayList<>());

        return tempTableName;
    }

    @Override
    public List<String> getSqlLog(String tempTableName) {
        validateTempTableName(tempTableName);

        // 校验是否有被挤占的情况
        checkNotEvicted(tempTableName);

        // 短期方案：如果内存中没有日志（服务可能重启过），但锁和临时表仍然存在，提示用户
        if (!sqlLogs.containsKey(tempTableName)) {
            EditLock lock = editLockMapper.selectByTempTable(tempTableName);
            if (lock != null && "LOCKED".equals(lock.getStatus())) {
                dropTempTable(tempTableName);
                throw new RuntimeException("后端服务曾经重启过，您此次编辑的操作记录已丢失，临时表已被自动清理。请刷新页面重新编辑！");
            }
        }

        return sqlLogs.getOrDefault(tempTableName, Collections.emptyList());
    }

    @Override
    public void logSql(String tempTableName, String sql) {
        sqlLogs.computeIfAbsent(tempTableName, k -> new ArrayList<>()).add(sql);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> commitSession(String tempTableName) {
        validateTempTableName(tempTableName);

        // 1. 校验锁状态
        EditLock lock = editLockMapper.selectByTempTable(tempTableName);
        if (lock == null || !"LOCKED".equals(lock.getStatus())) {
            throw new RuntimeException("锁已失效或不存在该会话");
        }

        // 2. 校验是否有被挤占的情况（超时后被别人解锁）
        checkNotEvicted(tempTableName);

        // 3. 直接从内存获取日志（避免 getSqlLog 重复校验）
        List<String> logs = sqlLogs.getOrDefault(tempTableName, Collections.emptyList());
        int successCount = 0;

        // 2. 回放 SQL 到正式表
        for (String sql : logs) {
            // 将 SQL 中的临时表名替换为正式表名
            String formalSql = sql.replace("INTO " + tempTableName, "INTO perm_menu")
                                  .replace("UPDATE " + tempTableName, "UPDATE perm_menu")
                                  .replace("FROM " + tempTableName, "FROM perm_menu");
            jdbcTemplate.execute(formalSql);
            successCount++;
        }

        // 3. 一致性校验（仅校验对应子系统范围内的数据）
        String subsystemCode = lock.getSubsystemCode();
        List<PermMenu> formalList = permMenuMapper.selectAllBySubsystem("perm_menu", subsystemCode);
        List<PermMenu> tempList = permMenuMapper.selectAllBySubsystem(tempTableName, subsystemCode);

        Map<String, PermMenu> formalMap = new HashMap<>();
        for (PermMenu menu : formalList) {
            formalMap.put(menu.getMenuCode() + "_" + menu.getMenuScope() + "_" + menu.getTenantId(), menu);
        }

        Map<String, PermMenu> tempMap = new HashMap<>();
        for (PermMenu menu : tempList) {
            tempMap.put(menu.getMenuCode() + "_" + menu.getMenuScope() + "_" + menu.getTenantId(), menu);
        }

        List<String> diffList = new ArrayList<>();

        for (String key : tempMap.keySet()) {
            if (!formalMap.containsKey(key)) {
                diffList.add("正式表缺少记录：" + key);
            } else {
                // 简化比对，可以进一步扩展到比对每个字段
                PermMenu formalMenu = formalMap.get(key);
                PermMenu tempMenu = tempMap.get(key);
                if (!Objects.equals(formalMenu.getMenuName(), tempMenu.getMenuName()) ||
                    !Objects.equals(formalMenu.getSortNo(), tempMenu.getSortNo()) ||
                    !Objects.equals(formalMenu.getMenuLevel(), tempMenu.getMenuLevel()) ||
                    !Objects.equals(formalMenu.getUppMenuCode(), tempMenu.getUppMenuCode())) {
                    diffList.add("记录存在差异：" + key + " (可能部分字段不一致)");
                }
            }
        }

        for (String key : formalMap.keySet()) {
            if (!tempMap.containsKey(key)) {
                diffList.add("正式表多出记录：" + key);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        if (!diffList.isEmpty()) {
            result.put("diff", diffList);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dropTempTable(String tempTableName) {
        validateTempTableName(tempTableName);
        String dropTableSql = "DROP TABLE IF EXISTS " + tempTableName;
        jdbcTemplate.execute(dropTableSql);
        editLockMapper.releaseLock(tempTableName);
        sqlLogs.remove(tempTableName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSession(String tempTableName) {
        dropTempTable(tempTableName);
    }
}
