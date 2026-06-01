package com.example.permmenu.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编辑锁实体类
 * 对应 perm_menu_edit_lock 表，用于并发编辑校验
 */
@Data
public class EditLock {

    /** 主键 */
    private Long id;

    /** 锁定人标识（浏览器端 UUID） */
    private String lockedBy;

    /** 锁定时间 */
    private LocalDateTime lockedAt;

    /** 对应临时表名 */
    private String tempTableName;

    /** 状态：LOCKED / RELEASED */
    private String status;

    /** 子系统编码过滤条件 */
    private String subsystemCode;
}
