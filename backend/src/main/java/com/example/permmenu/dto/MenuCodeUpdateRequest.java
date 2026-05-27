package com.example.permmenu.dto;

import lombok.Data;

/**
 * 菜单编码修改请求 DTO
 */
@Data
public class MenuCodeUpdateRequest {

    /** 旧菜单编码 */
    private String oldMenuCode;

    /** 新菜单编码 */
    private String newMenuCode;

    /** 菜单渠道 */
    private String menuScope;

    /** 租户号 */
    private String tenantId;
}
