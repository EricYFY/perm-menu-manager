package com.example.permmenu.service.impl;

import com.example.permmenu.dto.MenuCodeUpdateRequest;
import com.example.permmenu.dto.MenuDragRequest;
import com.example.permmenu.dto.MenuTreeNode;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.mapper.PermMenuMapper;
import com.example.permmenu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final PermMenuMapper permMenuMapper;

    /**
     * 获取菜单树
     * 查询指定渠道的所有菜单 → 内存构建树（根节点 UPP_MENU_CODE 为 null 或空）→ 按 SORT_NO 排序
     */
    @Override
    public List<MenuTreeNode> getMenuTree(String menuScope, String tenantId) {
        // 查询指定渠道的所有菜单
        List<PermMenu> menuList = permMenuMapper.selectByScope(menuScope, tenantId);

        // 转换为树节点
        List<MenuTreeNode> nodeList = menuList.stream()
                .map(this::convertToTreeNode)
                .collect(Collectors.toList());

        // 构建编码到节点的映射
        Map<String, MenuTreeNode> nodeMap = new LinkedHashMap<>();
        for (MenuTreeNode node : nodeList) {
            nodeMap.put(node.getMenuCode(), node);
        }

        // 构建树结构
        List<MenuTreeNode> rootNodes = new ArrayList<>();
        for (MenuTreeNode node : nodeList) {
            String uppMenuCode = node.getUppMenuCode();
            if (uppMenuCode == null || uppMenuCode.trim().isEmpty()) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 查找父节点并挂载
                MenuTreeNode parent = nodeMap.get(uppMenuCode);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    // 父节点不存在，作为根节点处理
                    rootNodes.add(node);
                }
            }
        }

        // 递归按 SORT_NO 排序
        sortTreeNodes(rootNodes);

        return rootNodes;
    }

    /**
     * 新增菜单
     * 校验主键唯一性 → 设置默认值 → 插入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(PermMenu menu) {
        // 校验主键唯一性
        PermMenu existing = permMenuMapper.selectByKey(
                menu.getMenuCode(), menu.getMenuScope(), menu.getTenantId());
        if (existing != null) {
            throw new RuntimeException("菜单编码已存在：" + menu.getMenuCode());
        }

        // 如果新增的是接口（menuKind=1 或 menuLevel=9），强制设其为 9 级
        if ("1".equals(menu.getMenuKind()) || (menu.getMenuLevel() != null && menu.getMenuLevel() == 9)) {
            menu.setMenuLevel(9L);
        }

        // 设置默认值
        setDefaultValues(menu);

        // 插入记录
        permMenuMapper.insertMenu(menu);
    }

    /**
     * 更新菜单（不允许修改主键字段）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(PermMenu menu) {
        // 校验菜单是否存在
        PermMenu existing = permMenuMapper.selectByKey(
                menu.getMenuCode(), menu.getMenuScope(), menu.getTenantId());
        if (existing == null) {
            throw new RuntimeException("菜单不存在：" + menu.getMenuCode());
        }

        // 如果该菜单是接口（原本是 9 级，或者新/旧的 menuKind 是 "1"），强制其 menuLevel 必须是 9
        if ("1".equals(existing.getMenuKind()) || "1".equals(menu.getMenuKind()) ||
                existing.getMenuLevel() == 9 || (menu.getMenuLevel() != null && menu.getMenuLevel() == 9)) {
            menu.setMenuLevel(9L);
        }

        // 更新记录
        permMenuMapper.updateByKey(menu);
    }

    /**
     * 修改菜单编码（级联更新）
     * 事务操作：校验新编码不冲突 → INSERT 新记录 → UPDATE 子菜单 UPP_MENU_CODE → DELETE 旧记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuCode(MenuCodeUpdateRequest request) {
        String oldCode = request.getOldMenuCode();
        String newCode = request.getNewMenuCode();
        String menuScope = request.getMenuScope();
        String tenantId = request.getTenantId();

        // 校验旧菜单是否存在
        PermMenu oldMenu = permMenuMapper.selectByKey(oldCode, menuScope, tenantId);
        if (oldMenu == null) {
            throw new RuntimeException("原菜单不存在：" + oldCode);
        }

        // 校验新编码是否已存在
        PermMenu existingNew = permMenuMapper.selectByKey(newCode, menuScope, tenantId);
        if (existingNew != null) {
            throw new RuntimeException("新菜单编码已存在：" + newCode);
        }

        // 1. INSERT 新记录（复制所有字段，使用新编码）
        PermMenu newMenu = new PermMenu();
        BeanUtils.copyProperties(oldMenu, newMenu);
        newMenu.setMenuCode(newCode);
        permMenuMapper.insertMenu(newMenu);

        // 2. UPDATE 子菜单的 UPP_MENU_CODE
        permMenuMapper.updateUppMenuCode(oldCode, newCode, menuScope, tenantId);

        // 3. DELETE 旧记录
        permMenuMapper.deleteByKey(oldCode, menuScope, tenantId);
    }

    /**
     * 拖拽移动菜单
     * 更新 UPP_MENU_CODE 和 MENU_LEVEL → 递归更新子菜单 MENU_LEVEL
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dragMenu(MenuDragRequest request) {
        String menuCode = request.getMenuCode();
        String newUppMenuCode = request.getNewUppMenuCode();
        String menuScope = request.getMenuScope();
        String tenantId = request.getTenantId();

        // 查询被拖拽的菜单
        PermMenu menu = permMenuMapper.selectByKey(menuCode, menuScope, tenantId);
        if (menu == null) {
            throw new RuntimeException("菜单不存在：" + menuCode);
        }

        // 计算新的菜单级别
        long newLevel;
        if ("1".equals(menu.getMenuKind()) || menu.getMenuLevel() == 9) {
            // 如果原本是接口（menuKind=1 或 原级别=9），保持为 9 级
            newLevel = 9;
        } else if (newUppMenuCode == null || newUppMenuCode.trim().isEmpty()) {
            // 移到根节点
            newLevel = 1;
        } else {
            // 查询新父节点
            PermMenu parentMenu = permMenuMapper.selectByKey(newUppMenuCode, menuScope, tenantId);
            if (parentMenu == null) {
                throw new RuntimeException("目标父菜单不存在：" + newUppMenuCode);
            }
            newLevel = parentMenu.getMenuLevel() + 1;
        }

        // 计算级别差值
        long levelDiff = newLevel - menu.getMenuLevel();

        // 更新当前菜单的 UPP_MENU_CODE 和 MENU_LEVEL
        menu.setUppMenuCode(newUppMenuCode);
        menu.setMenuLevel(newLevel);
        permMenuMapper.updateByKey(menu);

        // 递归更新子菜单的 MENU_LEVEL
        if (levelDiff != 0) {
            updateChildrenLevel(menuCode, menuScope, tenantId, levelDiff);
        }
    }

    /**
     * 删除菜单（级联删除所有后代）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(String menuCode, String menuScope, String tenantId) {
        // 校验菜单是否存在
        PermMenu menu = permMenuMapper.selectByKey(menuCode, menuScope, tenantId);
        if (menu == null) {
            throw new RuntimeException("菜单不存在：" + menuCode);
        }

        // 递归查询所有后代菜单
        List<PermMenu> descendants = permMenuMapper.selectAllDescendants(menuCode, menuScope, tenantId);

        // 删除所有后代菜单
        for (PermMenu descendant : descendants) {
            permMenuMapper.deleteByKey(descendant.getMenuCode(), menuScope, tenantId);
        }

        // 删除自身
        permMenuMapper.deleteByKey(menuCode, menuScope, tenantId);
    }

    /**
     * 递归更新子菜单的 MENU_LEVEL
     *
     * @param parentCode 父菜单编码
     * @param menuScope  菜单渠道
     * @param tenantId   租户号
     * @param levelDiff  级别差值
     */
    private void updateChildrenLevel(String parentCode, String menuScope,
                                     String tenantId, long levelDiff) {
        List<PermMenu> children = permMenuMapper.selectChildren(parentCode, menuScope, tenantId);
        for (PermMenu child : children) {
            // 如果子节点本身是 9 级接口，层级永远保持为 9，不随父节点移动而改变
            if (child.getMenuLevel() == 9) {
                continue;
            }
            child.setMenuLevel(child.getMenuLevel() + levelDiff);
            permMenuMapper.updateByKey(child);
            // 递归更新子菜单
            updateChildrenLevel(child.getMenuCode(), menuScope, tenantId, levelDiff);
        }
    }

    /**
     * 将实体转换为树节点
     */
    private MenuTreeNode convertToTreeNode(PermMenu menu) {
        MenuTreeNode node = new MenuTreeNode();
        BeanUtils.copyProperties(menu, node);
        node.setChildren(new ArrayList<>());
        return node;
    }

    /**
     * 递归按 SORT_NO 排序树节点
     */
    private void sortTreeNodes(List<MenuTreeNode> nodes) {
        nodes.sort(Comparator.comparing(
                MenuTreeNode::getSortNo,
                Comparator.nullsLast(String::compareTo)
        ));
        for (MenuTreeNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeNodes(node.getChildren());
            }
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(PermMenu menu) {
        if (menu.getTenantId() == null || menu.getTenantId().isEmpty()) {
            menu.setTenantId("047");
        }
        if (menu.getStat() == null || menu.getStat().isEmpty()) {
            menu.setStat("1");
        }
        if (menu.getCtrlAtti() == null || menu.getCtrlAtti().isEmpty()) {
            menu.setCtrlAtti("000000000100000");
        }
        if (menu.getTbVersion() == null || menu.getTbVersion().isEmpty()) {
            menu.setTbVersion("3.0.0");
        }
        if (menu.getMenuAttribute() == null || menu.getMenuAttribute().isEmpty()) {
            menu.setMenuAttribute("10000000");
        }
    }
}
