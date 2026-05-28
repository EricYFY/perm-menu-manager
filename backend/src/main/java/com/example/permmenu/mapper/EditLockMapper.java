package com.example.permmenu.mapper;

import com.example.permmenu.entity.EditLock;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 编辑锁 Mapper 接口
 */
public interface EditLockMapper {

    /**
     * 查询所有活跃的锁记录（STATUS = 'LOCKED' 且未超时）
     *
     * @param timeoutHours 超时小时数
     * @return 活跃锁列表
     */
    List<EditLock> selectActiveLocks(@Param("timeoutHours") int timeoutHours);

    /**
     * 插入锁记录
     *
     * @param lock 锁实体
     * @return 受影响行数
     */
    int insertLock(@Param("lock") EditLock lock);

    /**
     * 释放锁（将状态改为 RELEASED）
     *
     * @param tempTableName 临时表名
     * @return 受影响行数
     */
    int releaseLock(@Param("tempTableName") String tempTableName);

    /**
     * 根据临时表名查询锁记录
     *
     * @param tempTableName 临时表名
     * @return 锁实体
     */
    EditLock selectByTempTable(@Param("tempTableName") String tempTableName);
}
