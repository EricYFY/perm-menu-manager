package com.example.permmenu.dto;

import lombok.Data;

/**
 * 菜单加挂的产品功能信息 VO
 */
@Data
public class MenuFeatureMountVO {
    private String tenantId;
    private String menuScope;
    private String menuCode;
    private String menuName;
    private String prodCode;
    private String prodName;
    private String featureId;
    private String featureName;
}
