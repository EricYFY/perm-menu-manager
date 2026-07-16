package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * PERM_PROD 权限产品信息表实体类
 */
@Data
@TableName("perm_prod")
public class PermProd {
    private String tenantId;
    private String stat;
    private String prodCode;
    private String prodName;
    private String prodType;
    private String prodGroupNo;
    private String prodGroupName;
    private String prodChannel;
    private String extProdCode;
    private String extProdName;
    private String prodFlags;
    private String webProdStat;
    private Date webListingDate;
    private Date webDelistingDate;
    private String appProdStat;
    private Date appListingDate;
    private Date appDelistingDate;
    private String summary;
    private String webDelistingReason;
    private String appDelistingReason;
    private Date creDate;
    private Date updDate;
}
