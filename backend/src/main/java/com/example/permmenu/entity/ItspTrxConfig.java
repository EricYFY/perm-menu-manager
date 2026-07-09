package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("itsp_trx_config")
public class ItspTrxConfig {

    @TableField("PRODUCT_CODE")
    private String productCode;

    @TableField("PRODUCT_NAME")
    private String productName;

    @TableField("BUSI_TYPE")
    private String busiType;

    @TableField("BUSI_NAME")
    private String busiName;

    // 复合主键部分1
    @TableField("TR_CODE")
    private String trCode;

    @TableField("TR_NAME")
    private String trName;

    @TableField("TR_CCY_FLD")
    private String trCcyFld;

    @TableField("TR_AMT_FLD")
    private String trAmtFld;

    // 复合主键部分2
    @TableField("LANGUAGE")
    private String language;

    @TableField("TR_REF_FLD")
    private String trRefFld;

    @TableField("TR_CUST_NAME_FLD")
    private String trCustNameFld;

    @TableField("TR_CUST_ACCT_FLD")
    private String trCustAcctFld;

    @TableField("TR_BIC_FLD")
    private String trBicFld;

    @TableField("CUST_ACCT_FLD")
    private String custAcctFld;

    @TableField("IMAGE_NO_FLD")
    private String imageNoFld;

    @TableField("SIGN_ID_FLD")
    private String signIdFld;

    @TableField("FEATURE_FLD")
    private String featureFld;

    @TableField("CONDITION_EXPR_FLD")
    private String conditionExprFld;
}
