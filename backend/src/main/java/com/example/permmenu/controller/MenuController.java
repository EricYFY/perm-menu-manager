package com.example.permmenu.controller;

import com.example.permmenu.dto.*;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取菜单树
     *
     * @param menuScope 菜单渠道（11-PC端，12-APP端）
     * @param tenantId  租户号，默认 047
     * @return 菜单树
     */
    @GetMapping("/tree/{menuScope}")
    public ResultVO<List<MenuTreeNode>> getMenuTree(
            @PathVariable String menuScope,
            @RequestParam(defaultValue = "047") String tenantId) {
        try {
            List<MenuTreeNode> tree = menuService.getMenuTree(menuScope, tenantId);
            return ResultVO.success(tree);
        } catch (Exception e) {
            return ResultVO.error("获取菜单树失败：" + e.getMessage());
        }
    }

    /**
     * 新增菜单
     *
     * @param menu 菜单实体
     * @return 操作结果
     */
    @PostMapping
    public ResultVO<Void> addMenu(@RequestBody PermMenu menu) {
        try {
            menuService.addMenu(menu);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("新增菜单失败：" + e.getMessage());
        }
    }

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     * @return 操作结果
     */
    @PutMapping
    public ResultVO<Void> updateMenu(@RequestBody PermMenu menu) {
        try {
            menuService.updateMenu(menu);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("更新菜单失败：" + e.getMessage());
        }
    }

    /**
     * 修改菜单编码（级联更新子菜单的上级编码）
     *
     * @param request 菜单编码修改请求
     * @return 操作结果
     */
    @PutMapping("/code")
    public ResultVO<Void> updateMenuCode(@RequestBody MenuCodeUpdateRequest request) {
        try {
            menuService.updateMenuCode(request);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("修改菜单编码失败：" + e.getMessage());
        }
    }

    /**
     * 拖拽移动菜单
     *
     * @param request 拖拽请求
     * @return 操作结果
     */
    @PutMapping("/drag")
    public ResultVO<Void> dragMenu(@RequestBody MenuDragRequest request) {
        try {
            menuService.dragMenu(request);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("拖拽菜单失败：" + e.getMessage());
        }
    }

    /**
     * 删除菜单（级联删除所有子菜单）
     *
     * @param menuScope 菜单渠道
     * @param menuCode  菜单编码
     * @param tenantId  租户号，默认 047
     * @return 操作结果
     */
    @DeleteMapping("/{menuScope}/{menuCode}")
    public ResultVO<Void> deleteMenu(
            @PathVariable String menuScope,
            @PathVariable String menuCode,
            @RequestParam(defaultValue = "047") String tenantId) {
        try {
            menuService.deleteMenu(menuCode, menuScope, tenantId);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("删除菜单失败：" + e.getMessage());
        }
    }
}
