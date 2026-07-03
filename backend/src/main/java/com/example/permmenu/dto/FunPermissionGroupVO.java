package com.example.permmenu.dto;

import lombok.Data;

@Data
public class FunPermissionGroupVO {
    private String transModule;
    private String transModuleDesc;
    private String busiType;
    private String busiTypeDesc;
    private String userRole;
    private String userRoleDesc;
    private Long btnCount;
}
