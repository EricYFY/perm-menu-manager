package com.example.permmenu.dto;

import lombok.Data;

/**
 * 产品功能信息 DTO/VO
 */
@Data
public class ProdFeatureVO {
    private String tenantId;
    private String prodCode;
    private String prodName;
    private String featureId;
    private String featureName;
    private String jobKinds;
}
