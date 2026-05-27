package com.example.permmenu.dto;

import lombok.Data;

/**
 * 菜单拖拽请求 DTO
 */
@Data
public class MenuDragRequest {

    /** 被拖拽的菜单编码 */
    private String menuCode;

    /** 新的上级菜单编码（为 null 或空表示移到根节点） */
    private String newUppMenuCode;

    /** 菜单渠道 */
    private String menuScope;

    /** 租户号 */
    private String tenantId;
}
