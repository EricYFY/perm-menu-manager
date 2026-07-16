package com.example.permmenu.service;

import com.example.permmenu.dto.MenuCodeUpdateRequest;
import com.example.permmenu.dto.MenuDragRequest;
import com.example.permmenu.dto.MenuFeatureMountVO;
import com.example.permmenu.dto.MenuTreeNode;
import com.example.permmenu.dto.ProdFeatureVO;
import com.example.permmenu.entity.PermFeatureMenu;
import com.example.permmenu.entity.PermMenu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 获取菜单树
     *
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @param tableName 表名（正式表或临时表）
     * @param subsystemCode 子系统编码
     * @return 树状结构列表
     */
    List<MenuTreeNode> getMenuTree(String menuScope, String tenantId, String tableName, String subsystemCode);

    /**
     * 新增菜单
     *
     * @param menu      菜单实体
     * @param tableName 表名
     */
    void addMenu(PermMenu menu, String tableName);

    /**
     * 更新菜单（不允许修改主键字段）
     *
     * @param menu      菜单实体
     * @param tableName 表名
     */
    void updateMenu(PermMenu menu, String tableName);

    /**
     * 修改菜单编码（级联更新子菜单的上级编码）
     *
     * @param request   菜单编码修改请求
     * @param tableName 表名
     */
    void updateMenuCode(MenuCodeUpdateRequest request, String tableName);

    /**
     * 拖拽移动菜单
     *
     * @param request   拖拽请求
     * @param tableName 表名
     */
    void dragMenu(MenuDragRequest request, String tableName);

    /**
     * 删除菜单（级联删除所有后代）
     *
     * @param menuCode  菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @param tableName 表名
     */
    void deleteMenu(String menuCode, String menuScope, String tenantId, String tableName);

    /**
     * 查询指定菜单被加挂的产品与功能列表
     *
     * @param menuScope 菜单渠道
     * @param menuCode  菜单编码
     * @param tenantId  租户号
     * @return 加挂的产品功能 VO 列表
     */
    List<MenuFeatureMountVO> getFeatureMounts(String menuScope, String menuCode, String tenantId);

    /**
     * 查询产品功能列表（支持模糊匹配）
     *
     * @param tenantId 租户号
     * @param keyword  关键词
     * @return 产品功能列表
     */
    List<ProdFeatureVO> getProdFeatures(String tenantId, String keyword);

    /**
     * 新增菜单功能加挂
     *
     * @param request 加挂请求对象
     */
    void addFeatureMount(PermFeatureMenu request);

    /**
     * 删除菜单功能加挂
     */
    void deleteFeatureMount(String menuScope, String menuCode, String prodCode, String featureId, String tenantId);
}
