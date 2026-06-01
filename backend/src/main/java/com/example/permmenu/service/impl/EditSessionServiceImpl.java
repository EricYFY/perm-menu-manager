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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unlockSession(String lockedBy, String subsystemCode) {
        // 1. 校验是否有活跃锁
        EditLock activeLock = getActiveLock();
        if (activeLock != null) {
            throw new RuntimeException("当前已有用户 " + activeLock.getLockedBy() + " 正在编辑中，开始时间：" + activeLock.getLockedAt());
        }

        // 2. 生成临时表名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String tempTableName = "perm_menu_" + timestamp;

        // 3. 创建临时表（仅复制指定子系统数据）
        String createTableSql = "CREATE TABLE " + tempTableName + " AS SELECT * FROM perm_menu WHERE SUBSYSTEM_CODE = '" + subsystemCode + "'";
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
        return sqlLogs.getOrDefault(tempTableName, Collections.emptyList());
    }

    @Override
    public void logSql(String tempTableName, String sql) {
        sqlLogs.computeIfAbsent(tempTableName, k -> new ArrayList<>()).add(sql);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> commitSession(String tempTableName) {
        // 1. 校验锁状态
        EditLock lock = editLockMapper.selectByTempTable(tempTableName);
        if (lock == null || !"LOCKED".equals(lock.getStatus())) {
            throw new RuntimeException("锁已失效或不存在该会话");
        }

        List<String> logs = getSqlLog(tempTableName);
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
