package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("itsp_fun_permission")
public class ItspFunPermission {
    @TableId(value = "permission_id", type = IdType.INPUT)
    private String permissionId;
    @TableField("trans_module")
    private String transModule;
    @TableField("trans_module_desc")
    private String transModuleDesc;
    @TableField("busi_type")
    private String busiType;
    @TableField("busi_type_desc")
    private String busiTypeDesc;
    @TableField("user_role")
    private String userRole;
    @TableField("user_role_desc")
    private String userRoleDesc;
    @TableField("fun_button")
    private String funButton;
    @TableField("fun_button_desc")
    private String funButtonDesc;
    @TableField("data_status")
    private String dataStatus;
    @TableField("remark")
    private String remark;
    @TableField("seq")
    private Integer seq;
    @TableField("condition_expr")
    private String conditionExpr;
}
