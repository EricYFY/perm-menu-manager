package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * PERM_FEATURE_MENU 菜单功能加挂关系表实体类
 */
@Data
@TableName("perm_feature_menu")
public class PermFeatureMenu {
    private String tenantId;
    private String stat;
    private String prodCode;
    private String featureId;
    private String menuScope;
    private String menuCode;
    private String menuName;
    private Date creDate;
    private Date updDate;
}
