package com.example.permmenu.service.impl;

import com.example.permmenu.dto.MenuCodeUpdateRequest;
import com.example.permmenu.dto.MenuDragRequest;
import com.example.permmenu.dto.MenuTreeNode;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.mapper.PermMenuMapper;
import com.example.permmenu.service.EditSessionService;
import com.example.permmenu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
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
    // 使用 Lazy 避免循环依赖（如有）
    @Lazy
    private final EditSessionService editSessionService;

    /**
     * 获取菜单树
     */
    @Override
    public List<MenuTreeNode> getMenuTree(String menuScope, String tenantId, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }
        
        List<PermMenu> menuList = permMenuMapper.selectByScope(menuScope, tenantId, tableName);

        List<MenuTreeNode> nodeList = menuList.stream()
                .map(this::convertToTreeNode)
                .collect(Collectors.toList());

        Map<String, MenuTreeNode> nodeMap = new LinkedHashMap<>();
        for (MenuTreeNode node : nodeList) {
            nodeMap.put(node.getMenuCode(), node);
        }

        List<MenuTreeNode> rootNodes = new ArrayList<>();
        for (MenuTreeNode node : nodeList) {
            String uppMenuCode = node.getUppMenuCode();
            if (uppMenuCode == null || uppMenuCode.trim().isEmpty()) {
                rootNodes.add(node);
            } else {
                MenuTreeNode parent = nodeMap.get(uppMenuCode);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    rootNodes.add(node);
                }
            }
        }

        sortTreeNodes(rootNodes);
        return rootNodes;
    }

    /**
     * 新增菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(PermMenu menu, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }
        
        PermMenu existing = permMenuMapper.selectByKey(
                menu.getMenuCode(), menu.getMenuScope(), menu.getTenantId(), tableName);
        if (existing != null) {
            throw new RuntimeException("菜单编码已存在：" + menu.getMenuCode());
        }

        if ("1".equals(menu.getMenuKind()) || (menu.getMenuLevel() != null && menu.getMenuLevel() == 9)) {
            menu.setMenuLevel(9L);
        }

        setDefaultValues(menu);

        // 如果是临时表，记录 SQL
        if (!"perm_menu".equals(tableName)) {
            String sql = buildInsertSql(menu, tableName);
            editSessionService.logSql(tableName, sql);
        }

        permMenuMapper.insertMenu(menu, tableName);
    }

    /**
     * 更新菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(PermMenu menu, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }
        
        PermMenu existing = permMenuMapper.selectByKey(
                menu.getMenuCode(), menu.getMenuScope(), menu.getTenantId(), tableName);
        if (existing == null) {
            throw new RuntimeException("菜单不存在：" + menu.getMenuCode());
        }

        if ("1".equals(existing.getMenuKind()) || "1".equals(menu.getMenuKind()) ||
                existing.getMenuLevel() == 9 || (menu.getMenuLevel() != null && menu.getMenuLevel() == 9)) {
            menu.setMenuLevel(9L);
        }

        if (!"perm_menu".equals(tableName)) {
            String sql = buildUpdateSql(menu, tableName);
            editSessionService.logSql(tableName, sql);
        }

        permMenuMapper.updateByKey(menu, tableName);
    }

    /**
     * 修改菜单编码（级联更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuCode(MenuCodeUpdateRequest request, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }
        
        String oldCode = request.getOldMenuCode();
        String newCode = request.getNewMenuCode();
        String menuScope = request.getMenuScope();
        String tenantId = request.getTenantId();

        PermMenu oldMenu = permMenuMapper.selectByKey(oldCode, menuScope, tenantId, tableName);
        if (oldMenu == null) {
            throw new RuntimeException("原菜单不存在：" + oldCode);
        }

        PermMenu existingNew = permMenuMapper.selectByKey(newCode, menuScope, tenantId, tableName);
        if (existingNew != null) {
            throw new RuntimeException("新菜单编码已存在：" + newCode);
        }

        PermMenu newMenu = new PermMenu();
        BeanUtils.copyProperties(oldMenu, newMenu);
        newMenu.setMenuCode(newCode);

        if (!"perm_menu".equals(tableName)) {
            editSessionService.logSql(tableName, buildInsertSql(newMenu, tableName));
            editSessionService.logSql(tableName, buildUpdateUppMenuCodeSql(oldCode, newCode, menuScope, tenantId, tableName));
            editSessionService.logSql(tableName, buildDeleteSql(oldCode, menuScope, tenantId, tableName));
        }

        permMenuMapper.insertMenu(newMenu, tableName);
        permMenuMapper.updateUppMenuCode(oldCode, newCode, menuScope, tenantId, tableName);
        permMenuMapper.deleteByKey(oldCode, menuScope, tenantId, tableName);
    }

    /**
     * 拖拽移动菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dragMenu(MenuDragRequest request, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }
        
        String menuCode = request.getMenuCode();
        String newUppMenuCode = request.getNewUppMenuCode();
        String menuScope = request.getMenuScope();
        String tenantId = request.getTenantId();

        PermMenu menu = permMenuMapper.selectByKey(menuCode, menuScope, tenantId, tableName);
        if (menu == null) {
            throw new RuntimeException("菜单不存在：" + menuCode);
        }

        long newLevel;
        if ("1".equals(menu.getMenuKind()) || menu.getMenuLevel() == 9) {
            newLevel = 9;
        } else if (newUppMenuCode == null || newUppMenuCode.trim().isEmpty()) {
            newLevel = 1;
        } else {
            PermMenu parentMenu = permMenuMapper.selectByKey(newUppMenuCode, menuScope, tenantId, tableName);
            if (parentMenu == null) {
                throw new RuntimeException("目标父菜单不存在：" + newUppMenuCode);
            }
            newLevel = parentMenu.getMenuLevel() + 1;
        }

        long levelDiff = newLevel - menu.getMenuLevel();

        menu.setUppMenuCode(newUppMenuCode);
        menu.setMenuLevel(newLevel);

        if (!"perm_menu".equals(tableName)) {
            editSessionService.logSql(tableName, buildUpdateSql(menu, tableName));
        }

        permMenuMapper.updateByKey(menu, tableName);

        if (levelDiff != 0) {
            updateChildrenLevel(menuCode, menuScope, tenantId, levelDiff, tableName);
        }
    }

    /**
     * 删除菜单（级联删除所有后代）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(String menuCode, String menuScope, String tenantId, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }
        
        PermMenu menu = permMenuMapper.selectByKey(menuCode, menuScope, tenantId, tableName);
        if (menu == null) {
            throw new RuntimeException("菜单不存在：" + menuCode);
        }

        List<PermMenu> descendants = permMenuMapper.selectAllDescendants(menuCode, menuScope, tenantId, tableName);

        for (PermMenu descendant : descendants) {
            if (!"perm_menu".equals(tableName)) {
                editSessionService.logSql(tableName, buildDeleteSql(descendant.getMenuCode(), menuScope, tenantId, tableName));
            }
            permMenuMapper.deleteByKey(descendant.getMenuCode(), menuScope, tenantId, tableName);
        }

        if (!"perm_menu".equals(tableName)) {
            editSessionService.logSql(tableName, buildDeleteSql(menuCode, menuScope, tenantId, tableName));
        }
        permMenuMapper.deleteByKey(menuCode, menuScope, tenantId, tableName);
    }

    private void updateChildrenLevel(String parentCode, String menuScope,
                                     String tenantId, long levelDiff, String tableName) {
        List<PermMenu> children = permMenuMapper.selectChildren(parentCode, menuScope, tenantId, tableName);
        for (PermMenu child : children) {
            if (child.getMenuLevel() == 9) {
                continue;
            }
            child.setMenuLevel(child.getMenuLevel() + levelDiff);
            
            if (!"perm_menu".equals(tableName)) {
                editSessionService.logSql(tableName, buildUpdateSql(child, tableName));
            }
            
            permMenuMapper.updateByKey(child, tableName);
            updateChildrenLevel(child.getMenuCode(), menuScope, tenantId, levelDiff, tableName);
        }
    }

    private MenuTreeNode convertToTreeNode(PermMenu menu) {
        MenuTreeNode node = new MenuTreeNode();
        BeanUtils.copyProperties(menu, node);
        node.setChildren(new ArrayList<>());
        return node;
    }

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

    private void setDefaultValues(PermMenu menu) {
        if (menu.getTenantId() == null || menu.getTenantId().isEmpty()) menu.setTenantId("047");
        if (menu.getStat() == null || menu.getStat().isEmpty()) menu.setStat("1");
        if (menu.getCtrlAtti() == null || menu.getCtrlAtti().isEmpty()) menu.setCtrlAtti("000000000100000");
        if (menu.getTbVersion() == null || menu.getTbVersion().isEmpty()) menu.setTbVersion("3.0.0");
        if (menu.getMenuAttribute() == null || menu.getMenuAttribute().isEmpty()) menu.setMenuAttribute("10000000");
    }

    // --- SQL Builder Methods for Audit Log ---

    private String escape(String val) {
        if (val == null) return "NULL";
        return "'" + val.replace("'", "''") + "'";
    }

    private String buildInsertSql(PermMenu menu, String tableName) {
        return String.format(
            "INSERT INTO %s (TENANT_ID, STAT, MENU_SCOPE, MENU_CODE, MENU_NAME, MENU_LEVEL, MENU_TYPE, UPP_MENU_CODE, MENU_CHECKED, MENU_KIND, MENU_VERIFY, MENU_DISPLAY, TR_CODE, SECURITY_TR_CODE, CTRL_ATTI, BIZ_ATTI, ACCT_AUTH_ATTI, ASAC_AUTH_ATTI, TIME_ATTI, WORKFLOW_FLAG, WORKFLOW_BIZ_TYPE, IS_ADMIN, IS_OPERATOR, IS_USER, SORT_NO, SUBSYSTEM_CODE, FOLDER_CODE, BIZ_CATEGORY_NO, BIZ_CATEGORY_NAME, MENU_ICON, MENU_HERF_TYPE, MENU_HERF, MENU_ATTRIBUTE, ICON_FLAG, IS_KEEP_ALIVE, JUMP_HERF, TB_VERSION, DESCRIPTION) VALUES (%s, %s, %s, %s, %s, %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);",
            tableName,
            escape(menu.getTenantId()), escape(menu.getStat()), escape(menu.getMenuScope()), escape(menu.getMenuCode()), escape(menu.getMenuName()), menu.getMenuLevel(), escape(menu.getMenuType()), escape(menu.getUppMenuCode()), escape(menu.getMenuChecked()), escape(menu.getMenuKind()), escape(menu.getMenuVerify()), escape(menu.getMenuDisplay()), escape(menu.getTrCode()), escape(menu.getSecurityTrCode()), escape(menu.getCtrlAtti()), escape(menu.getBizAtti()), escape(menu.getAcctAuthAtti()), escape(menu.getAsacAuthAtti()), escape(menu.getTimeAtti()), escape(menu.getWorkflowFlag()), escape(menu.getWorkflowBizType()), escape(menu.getIsAdmin()), escape(menu.getIsOperator()), escape(menu.getIsUser()), escape(menu.getSortNo()), escape(menu.getSubsystemCode()), escape(menu.getFolderCode()), escape(menu.getBizCategoryNo()), escape(menu.getBizCategoryName()), escape(menu.getMenuIcon()), escape(menu.getMenuHerfType()), escape(menu.getMenuHerf()), escape(menu.getMenuAttribute()), escape(menu.getIconFlag()), escape(menu.getIsKeepAlive()), escape(menu.getJumpHerf()), escape(menu.getTbVersion()), escape(menu.getDescription())
        );
    }

    private String buildUpdateSql(PermMenu menu, String tableName) {
        return String.format(
            "UPDATE %s SET MENU_NAME = %s, MENU_LEVEL = %d, MENU_TYPE = %s, UPP_MENU_CODE = %s, MENU_CHECKED = %s, MENU_KIND = %s, MENU_VERIFY = %s, MENU_DISPLAY = %s, TR_CODE = %s, SECURITY_TR_CODE = %s, CTRL_ATTI = %s, BIZ_ATTI = %s, ACCT_AUTH_ATTI = %s, ASAC_AUTH_ATTI = %s, TIME_ATTI = %s, WORKFLOW_FLAG = %s, WORKFLOW_BIZ_TYPE = %s, IS_ADMIN = %s, IS_OPERATOR = %s, IS_USER = %s, SORT_NO = %s, SUBSYSTEM_CODE = %s, FOLDER_CODE = %s, BIZ_CATEGORY_NO = %s, BIZ_CATEGORY_NAME = %s, MENU_ICON = %s, MENU_HERF_TYPE = %s, MENU_HERF = %s, MENU_ATTRIBUTE = %s, ICON_FLAG = %s, IS_KEEP_ALIVE = %s, JUMP_HERF = %s, STAT = %s, TB_VERSION = %s, DESCRIPTION = %s WHERE MENU_CODE = %s AND MENU_SCOPE = %s AND TENANT_ID = %s;",
            tableName,
            escape(menu.getMenuName()), menu.getMenuLevel(), escape(menu.getMenuType()), escape(menu.getUppMenuCode()), escape(menu.getMenuChecked()), escape(menu.getMenuKind()), escape(menu.getMenuVerify()), escape(menu.getMenuDisplay()), escape(menu.getTrCode()), escape(menu.getSecurityTrCode()), escape(menu.getCtrlAtti()), escape(menu.getBizAtti()), escape(menu.getAcctAuthAtti()), escape(menu.getAsacAuthAtti()), escape(menu.getTimeAtti()), escape(menu.getWorkflowFlag()), escape(menu.getWorkflowBizType()), escape(menu.getIsAdmin()), escape(menu.getIsOperator()), escape(menu.getIsUser()), escape(menu.getSortNo()), escape(menu.getSubsystemCode()), escape(menu.getFolderCode()), escape(menu.getBizCategoryNo()), escape(menu.getBizCategoryName()), escape(menu.getMenuIcon()), escape(menu.getMenuHerfType()), escape(menu.getMenuHerf()), escape(menu.getMenuAttribute()), escape(menu.getIconFlag()), escape(menu.getIsKeepAlive()), escape(menu.getJumpHerf()), escape(menu.getStat()), escape(menu.getTbVersion()), escape(menu.getDescription()),
            escape(menu.getMenuCode()), escape(menu.getMenuScope()), escape(menu.getTenantId())
        );
    }

    private String buildDeleteSql(String menuCode, String menuScope, String tenantId, String tableName) {
        return String.format(
            "DELETE FROM %s WHERE MENU_CODE = %s AND MENU_SCOPE = %s AND TENANT_ID = %s;",
            tableName, escape(menuCode), escape(menuScope), escape(tenantId)
        );
    }

    private String buildUpdateUppMenuCodeSql(String oldCode, String newCode, String menuScope, String tenantId, String tableName) {
        return String.format(
            "UPDATE %s SET UPP_MENU_CODE = %s WHERE UPP_MENU_CODE = %s AND MENU_SCOPE = %s AND TENANT_ID = %s;",
            tableName, escape(newCode), escape(oldCode), escape(menuScope), escape(tenantId)
        );
    }
}
