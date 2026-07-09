package com.example.permmenu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜单权限实体类
 * 对应 perm_menu 表，复合主键为 MENU_CODE + MENU_SCOPE + TENANT_ID
 * 由于是复合主键，不使用 MyBatis-Plus 的 @TableId，使用自定义 SQL 操作
 */
@Data
@TableName("perm_menu")
public class PermMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户号 */
    @TableField("TENANT_ID")
    private String tenantId;

    /** 记录状态 */
    @TableField("STAT")
    private String stat;

    /** 菜单渠道 */
    @TableField("MENU_SCOPE")
    private String menuScope;

    /** 菜单编码 */
    @TableField("MENU_CODE")
    private String menuCode;

    /** 菜单名称 */
    @TableField("MENU_NAME")
    private String menuName;

    /** 菜单级别 */
    @TableField("MENU_LEVEL")
    private Long menuLevel;

    /** 菜单类型 */
    @TableField("MENU_TYPE")
    private String menuType;

    /** 上级菜单编码 */
    @TableField("UPP_MENU_CODE")
    private String uppMenuCode;

    /** 菜单选中 */
    @TableField("MENU_CHECKED")
    private String menuChecked;

    /** 菜单分类 0-菜单 1-事件 */
    @TableField("MENU_KIND")
    private String menuKind;

    /** 权限校验 */
    @TableField("MENU_VERIFY")
    private String menuVerify;

    /** 菜单显示 */
    @TableField("MENU_DISPLAY")
    private String menuDisplay;

    /** 服务码 */
    @TableField("TR_CODE")
    private String trCode;

    /** 安全验证判断服务码 */
    @TableField("SECURITY_TR_CODE")
    private String securityTrCode;


    /** 服务控制属性 */
    @TableField("CTRL_ATTI")
    private String ctrlAtti;

    /** 服务业务属性 */
    @TableField("BIZ_ATTI")
    private String bizAtti;

    /** 账户授权属性 */
    @TableField("ACCT_AUTH_ATTI")
    private String acctAuthAtti;

    /** 账簿授权属性 */
    @TableField("ASAC_AUTH_ATTI")
    private String asacAuthAtti;

    /** 时间段属性 */
    @TableField("TIME_ATTI")
    private String timeAtti;

    /** 审批标志 */
    @TableField("WORKFLOW_FLAG")
    private String workflowFlag;

    /** 审批业务类型 */
    @TableField("WORKFLOW_BIZ_TYPE")
    private String workflowBizType;

    /** 是否管理员菜单 */
    @TableField("IS_ADMIN")
    private String isAdmin;

    /** 是否操作员菜单 */
    @TableField("IS_OPERATOR")
    private String isOperator;

    /** 是否普通用户菜单 */
    @TableField("IS_USER")
    private String isUser;

    /** 排序编号 */
    @TableField("SORT_NO")
    private String sortNo;

    /** 系统编码 */
    @TableField("SUBSYSTEM_CODE")
    private String subsystemCode;

    /** 文件夹编码 */
    @TableField("FOLDER_CODE")
    private String folderCode;

    /** 业务分类编号 */
    @TableField("BIZ_CATEGORY_NO")
    private String bizCategoryNo;

    /** 业务分类名称 */
    @TableField("BIZ_CATEGORY_NAME")
    private String bizCategoryName;

    /** 菜单图标 */
    @TableField("MENU_ICON")
    private String menuIcon;

    /** 菜单链接类型 */
    @TableField("MENU_HERF_TYPE")
    private String menuHerfType;

    /** 菜单链接 */
    @TableField("MENU_HERF")
    private String menuHerf;

    /** 菜单属性 */
    @TableField("MENU_ATTRIBUTE")
    private String menuAttribute;

    /** 是否使用新图标库 */
    @TableField("ICON_FLAG")
    private String iconFlag;

    /** 页面是否缓存 */
    @TableField("IS_KEEP_ALIVE")
    private String isKeepAlive;

    /** 跳转访问地址 */
    @TableField("JUMP_HERF")
    private String jumpHerf;

    /** 版本号 */
    @TableField("TB_VERSION")
    private String tbVersion;

    /** 描述 */
    @TableField("DESCRIPTION")
    private String description;
}
