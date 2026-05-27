package com.example.permmenu.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点 DTO
 * 包含所有菜单字段以及子节点列表
 */
@Data
public class MenuTreeNode {

    /** 租户号 */
    private String tenantId;

    /** 记录状态 */
    private String stat;

    /** 菜单渠道 */
    private String menuScope;

    /** 菜单编码 */
    private String menuCode;

    /** 菜单名称 */
    private String menuName;

    /** 菜单级别 */
    private Long menuLevel;

    /** 菜单类型 */
    private String menuType;

    /** 上级菜单编码 */
    private String uppMenuCode;

    /** 菜单选中 */
    private String menuChecked;

    /** 菜单分类 0-菜单 1-事件 */
    private String menuKind;

    /** 权限校验 */
    private String menuVerify;

    /** 菜单显示 */
    private String menuDisplay;

    /** 服务码 */
    private String trCode;

    /** 安全验证判断服务码 */
    private String securityTrCode;

    /** 服务控制属性 */
    private String ctrlAtti;

    /** 服务业务属性 */
    private String bizAtti;

    /** 账户授权属性 */
    private String acctAuthAtti;

    /** 账簿授权属性 */
    private String asacAuthAtti;

    /** 时间段属性 */
    private String timeAtti;

    /** 审批标志 */
    private String workflowFlag;

    /** 审批业务类型 */
    private String workflowBizType;

    /** 是否管理员菜单 */
    private String isAdmin;

    /** 是否操作员菜单 */
    private String isOperator;

    /** 是否普通用户菜单 */
    private String isUser;

    /** 排序编号 */
    private String sortNo;

    /** 系统编码 */
    private String subsystemCode;

    /** 文件夹编码 */
    private String folderCode;

    /** 业务分类编号 */
    private String bizCategoryNo;

    /** 业务分类名称 */
    private String bizCategoryName;

    /** 菜单图标 */
    private String menuIcon;

    /** 菜单链接类型 */
    private String menuHerfType;

    /** 菜单链接 */
    private String menuHerf;

    /** 菜单属性 */
    private String menuAttribute;

    /** 是否使用新图标库 */
    private String iconFlag;

    /** 页面是否缓存 */
    private String isKeepAlive;

    /** 跳转访问地址 */
    private String jumpHerf;

    /** 版本号 */
    private String tbVersion;

    /** 描述 */
    private String description;

    /** 子节点列表 */
    private List<MenuTreeNode> children = new ArrayList<>();
}
