package com.example.permmenu.service;

import com.example.permmenu.entity.EditLock;

import java.util.List;
import java.util.Map;

/**
 * 编辑会话服务接口
 */
public interface EditSessionService {

    /**
     * 获取当前的活跃锁状态
     *
     * @return 如果有活跃锁，返回锁实体，否则返回 null
     */
    EditLock getActiveLock();

    /**
     * 申请解锁编辑模式
     *
     * @param lockedBy 申请人唯一标识
     * @param subsystemCode 子系统编码
     * @return 临时表名
     */
    String unlockSession(String lockedBy, String subsystemCode);

    /**
     * 获取指定临时表的 SQL 日志
     *
     * @param tempTableName 临时表名
     * @return SQL 列表
     */
    List<String> getSqlLog(String tempTableName);

    /**
     * 记录 SQL 日志（由 MenuService 在操作临时表时调用）
     *
     * @param tempTableName 临时表名
     * @param sql           要记录的 SQL 语句
     */
    void logSql(String tempTableName, String sql);

    /**
     * 提交编辑（回放 SQL 并比较差异）
     *
     * @param tempTableName 临时表名
     * @return 包含结果信息的 Map，如果有差异则包含 "diff" 字段
     */
    Map<String, Object> commitSession(String tempTableName);

    /**
     * 删除临时表并释放锁
     *
     * @param tempTableName 临时表名
     */
    void dropTempTable(String tempTableName);

    /**
     * 放弃编辑会话
     *
     * @param tempTableName 临时表名
     */
    void cancelSession(String tempTableName);
}
