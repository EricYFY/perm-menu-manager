package com.example.permmenu.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * TBSP_JNL 平台交易流水与查询流水统一实体类
 */
@Data
public class TbspJnl {
    private String tenantId;
    private String jnlStat;
    private String bizStat;
    private String channelNo;
    private String custNo;
    private String organizationNo;
    private String userId;
    private String userNo;
    private String userName;
    private String trCode;
    private String prodGroupNo;
    private String prodCode;
    private String menuCode;
    private String menuName;
    private String trDate;
    private String trTime;
    private String serialNo;
    private String reqDate;
    private String reqTime;
    private String reqSerialNo;
    private String origDate;
    private String origTime;
    private String origSerialNo;
    private String referSerialNo; // jnl 表独有
    private String outSerialNo;   // jnl 表独有
    private String traceId;
    private String orderNo;       // jnl 表独有
    private String assetNo;
    private String assetUnit;
    private BigDecimal amt;
    private String ip;
    private String mac;
    private String deviceNo;
    private String deviceName;
    private BigDecimal printTimes; // jnl 表独有
    private String respCode;
    private String respMsg;
    private String respExt;
    private String summary;
    private String extJson;
    private Date creDate;
    private Date updDate;
    private String userType;
    private String menuType;
}
