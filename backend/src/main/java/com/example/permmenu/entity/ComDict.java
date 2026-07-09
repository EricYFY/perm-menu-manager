package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典实体类
 * 对应 com_dict 表，复合主键为 DICT_ID + DICT_KEY + TENANT_ID
 */
@Data
@TableName("com_dict")
public class ComDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户号 */
    @TableField("TENANT_ID")
    private String tenantId;

    /** 状态 */
    @TableField("STAT")
    private String stat;

    /** 字典ID */
    @TableField("DICT_ID")
    private String dictId;

    /** 字典键值 */
    @TableField("DICT_KEY")
    private String dictKey;

    /** 字典值 */
    @TableField("DICT_VALUE")
    private String dictValue;

    /** 关联键值（用于现场维护） */
    @TableField("REL_KEY")
    private String relKey;

    /** 枚举键值 */
    @TableField("ENUM_KEY")
    private String enumKey;

    /** 场景 */
    @TableField("SCENE")
    private String scene;

    /** 排序编号 */
    @TableField("SORT_NO")
    private String sortNo;

    /** 说明 */
    @TableField("REMARK")
    private String remark;
}
