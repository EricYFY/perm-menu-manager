package com.example.permmenu.service;

import com.example.permmenu.dto.MenuCodeUpdateRequest;
import com.example.permmenu.dto.MenuDragRequest;
import com.example.permmenu.dto.MenuTreeNode;
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
     * @return 菜单树节点列表（根节点）
     */
    List<MenuTreeNode> getMenuTree(String menuScope, String tenantId);

    /**
     * 新增菜单
     *
     * @param menu 菜单实体
     */
    void addMenu(PermMenu menu);

    /**
     * 更新菜单（不允许修改主键字段）
     *
     * @param menu 菜单实体
     */
    void updateMenu(PermMenu menu);

    /**
     * 修改菜单编码（级联更新子菜单的上级编码）
     *
     * @param request 菜单编码修改请求
     */
    void updateMenuCode(MenuCodeUpdateRequest request);

    /**
     * 拖拽移动菜单
     *
     * @param request 拖拽请求
     */
    void dragMenu(MenuDragRequest request);

    /**
     * 删除菜单（级联删除所有后代）
     *
     * @param menuCode  菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     */
    void deleteMenu(String menuCode, String menuScope, String tenantId);
}
