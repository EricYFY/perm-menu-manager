package com.example.permmenu.entity;

import lombok.Data;
import java.util.Date;

/**
 * TBSP_DATA 流水数据表实体类
 */
@Data
public class TbspData {
    private String tenantId;
    private String custNo;
    private String serialNo;
    private String requestContext;
    private String responseContext;
    private String mirrorContext;
    private String certCn;
    private String signContent;
    private String signData;
    private Date creDate;
    private Date updDate;
}
