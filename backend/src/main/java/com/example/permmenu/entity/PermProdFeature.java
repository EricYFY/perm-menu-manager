package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * PERM_PROD_FEATURE 产品功能信息表实体类
 */
@Data
@TableName("perm_prod_feature")
public class PermProdFeature {
    private String tenantId;
    private String stat;
    private String prodCode;
    private String featureId;
    private String featureName;
    private String jobKinds;
    private Date creDate;
    private Date updDate;
}
