package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("itsp_flow_ump_config")
public class ItspFlowUmpConfig {
    @TableId(value = "CFG_ID", type = IdType.INPUT)
    private String cfgId;
    @TableField("TR_CODE")
    private String trCode;
    @TableField("TR_NAME")
    private String trName;
    @TableField("TR_DESC")
    private String trDesc;
    @TableField("OPER_COMMAND")
    private String operCommand;
    @TableField("FLOW_STATUS")
    private String flowStatus;
    @TableField("ORDER_STEP")
    private String orderStep;
    @TableField("NEXT_STEP")
    private String nextStep;
    @TableField("ESC_CODE")
    private String escCode;
    @TableField("EDIT_FLAG")
    private String editFlag;
    @TableField("EXCHANGE")
    private String exchange;
    @TableField("ROUTING_KEY")
    private String routingKey;
    @TableField("MSG_TYPE")
    private String msgType;
    @TableField("MSG_SYS_ID")
    private String msgSysId;
    @TableField("BUSINESS_TYPE")
    private String businessType;
    @TableField("LAYER_CODE")
    private String layerCode;
    @TableField("MER_DISE_CODE")
    private String merDiseCode;
    @TableField("FUNC_CODE")
    private String funcCode;
    @TableField("CHILD_NEXT_STEP")
    private String childNextStep;
    @TableField("CONDITION_EXPR")
    private String conditionExpr;
    @TableField("UMP_TENANTID")
    private String umpTenantid;
}
