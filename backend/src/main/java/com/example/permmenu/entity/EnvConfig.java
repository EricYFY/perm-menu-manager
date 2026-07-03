package com.example.permmenu.entity;

import lombok.Data;

@Data
public class EnvConfig {
    private String envId;
    private String envName;
    
    // master 库配置
    private String masterUrl;
    private String masterUsername;
    private String masterPassword;
    private String masterDriver = "com.mysql.cj.jdbc.Driver";
    
    // second 库配置
    private String secondUrl;
    private String secondUsername;
    private String secondPassword;
    private String secondDriver = "com.mysql.cj.jdbc.Driver";
}
