package com.example.permmenu.controller;

import com.example.permmenu.dto.*;
import com.example.permmenu.entity.PermFeatureMenu;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取菜单树
     *
     * @param menuScope     菜单渠道（11-PC端，12-APP端）
     * @param tenantId      租户号，默认 047
     * @param subsystemCode 子系统代码
     * @param tempTableName 临时表名
     * @return 菜单树
     */
    @GetMapping("/tree")
    public ResultVO<List<MenuTreeNode>> getMenuTree(@RequestParam String menuScope,
                                                    @RequestParam(defaultValue = "047") String tenantId,
                                                    @RequestParam(required = false) String subsystemCode,
                                                    @RequestHeader(value = "X-Temp-Table", required = false) String tempTableName) {
        String tableName = (tempTableName != null && !tempTableName.isEmpty()) ? tempTableName : "perm_menu";
        List<MenuTreeNode> tree = menuService.getMenuTree(menuScope, tenantId, tableName, subsystemCode);
        return ResultVO.success(tree);
    }

    /**
     * 新增菜单
     */
    @PostMapping({"", "/add"})
    public ResultVO<Void> addMenu(
            @RequestBody PermMenu menu,
            @RequestHeader(value = "X-Temp-Table", required = false) String tempTableName) {
        menuService.addMenu(menu, tempTableName);
        return ResultVO.success();
    }

    /**
     * 更新菜单（纯 POST 接口：/menu/update）
     */
    @PostMapping("/update")
    public ResultVO<Void> updateMenu(
            @RequestBody PermMenu menu,
            @RequestHeader(value = "X-Temp-Table", required = false) String tempTableName) {
        menuService.updateMenu(menu, tempTableName);
        return ResultVO.success();
    }

    /**
     * 修改菜单编码（纯 POST 接口：/menu/code）
     */
    @PostMapping("/code")
    public ResultVO<Void> updateMenuCode(
            @RequestBody MenuCodeUpdateRequest request,
            @RequestHeader(value = "X-Temp-Table", required = false) String tempTableName) {
        menuService.updateMenuCode(request, tempTableName);
        return ResultVO.success();
    }

    /**
     * 拖拽移动菜单（纯 POST 接口：/menu/drag）
     */
    @PostMapping("/drag")
    public ResultVO<Void> dragMenu(
            @RequestBody MenuDragRequest request,
            @RequestHeader(value = "X-Temp-Table", required = false) String tempTableName) {
        menuService.dragMenu(request, tempTableName);
        return ResultVO.success();
    }

    /**
     * 删除菜单（POST接口）
     */
    @PostMapping(value = {"/delete/{menuScope}/{menuCode}", "/{menuScope}/{menuCode}"})
    public ResultVO<Void> deleteMenu(
            @PathVariable String menuScope,
            @PathVariable String menuCode,
            @RequestParam(defaultValue = "047") String tenantId,
            @RequestHeader(value = "X-Temp-Table", required = false) String tempTableName) {
        menuService.deleteMenu(menuCode, menuScope, tenantId, tempTableName);
        return ResultVO.success();
    }

    /**
     * 查询指定菜单被加挂的产品与功能列表
     */
    @GetMapping("/feature-mounts")
    public ResultVO<List<MenuFeatureMountVO>> getFeatureMounts(
            @RequestParam String menuScope,
            @RequestParam String menuCode,
            @RequestParam(defaultValue = "047") String tenantId) {
        List<MenuFeatureMountVO> list = menuService.getFeatureMounts(menuScope, menuCode, tenantId);
        return ResultVO.success(list);
    }

    /**
     * 查询产品功能列表（支持模糊匹配 PROD_NAME 或 FEATURE_NAME）
     */
    @GetMapping("/prod-features")
    public ResultVO<List<ProdFeatureVO>> getProdFeatures(
            @RequestParam(defaultValue = "047") String tenantId,
            @RequestParam(required = false) String keyword) {
        List<ProdFeatureVO> list = menuService.getProdFeatures(tenantId, keyword);
        return ResultVO.success(list);
    }

    /**
     * 新增菜单功能加挂
     */
    @PostMapping("/feature-mount/add")
    public ResultVO<Void> addFeatureMount(@RequestBody PermFeatureMenu request) {
        menuService.addFeatureMount(request);
        return ResultVO.success();
    }

    /**
     * 删除菜单功能加挂
     */
    @PostMapping("/feature-mount/delete")
    public ResultVO<Void> deleteFeatureMount(
            @RequestParam String menuScope,
            @RequestParam String menuCode,
            @RequestParam String prodCode,
            @RequestParam String featureId,
            @RequestParam(defaultValue = "047") String tenantId) {
        menuService.deleteFeatureMount(menuScope, menuCode, prodCode, featureId, tenantId);
        return ResultVO.success();
    }
}
