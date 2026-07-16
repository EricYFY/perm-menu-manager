package com.example.permmenu.service.impl;

import com.example.permmenu.dto.MenuCodeUpdateRequest;
import com.example.permmenu.dto.MenuDragRequest;
import com.example.permmenu.dto.MenuFeatureMountVO;
import com.example.permmenu.dto.MenuTreeNode;
import com.example.permmenu.dto.ProdFeatureVO;
import com.example.permmenu.entity.PermFeatureMenu;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.mapper.PermFeatureMenuMapper;
import com.example.permmenu.mapper.PermMenuMapper;
import com.example.permmenu.mapper.PermProdFeatureMapper;
import com.example.permmenu.service.EditSessionService;
import com.example.permmenu.service.MenuService;
import com.example.permmenu.util.TableMetaUtil;
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
    private final PermFeatureMenuMapper permFeatureMenuMapper;
    private final PermProdFeatureMapper permProdFeatureMapper;
    // 使用 Lazy 避免循环依赖（如有）
    @Lazy
    private final EditSessionService editSessionService;

    /**
     * 获取菜单树
     */
    @Override
    public List<MenuTreeNode> getMenuTree(String menuScope, String tenantId, String tableName, String subsystemCode) {
        if (tableName == null || tableName.isEmpty()) {
            tableName = "perm_menu";
        }

        List<PermMenu> menuList = permMenuMapper.selectByScope(menuScope, tenantId, tableName, subsystemCode);

        Set<String> mountedMenuCodes;
        try {
            List<String> codes = permFeatureMenuMapper.selectMountedMenuCodes(menuScope, tenantId);
            mountedMenuCodes = (codes != null) ? new HashSet<>(codes) : Collections.emptySet();
        } catch (Exception e) {
            mountedMenuCodes = Collections.emptySet();
        }

        final Set<String> mountedSet = mountedMenuCodes;
        List<MenuTreeNode> nodeList = menuList.stream()
                .map(menu -> {
                    MenuTreeNode node = convertToTreeNode(menu);
                    node.setIsMounted(mountedSet.contains(menu.getMenuCode()));
                    return node;
                })
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
            editSessionService.logSql(tableName,
                    buildUpdateUppMenuCodeSql(oldCode, newCode, menuScope, tenantId, tableName));
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

        if (menuCode != null && menuCode.equals(newUppMenuCode)) {
            throw new RuntimeException("不能将菜单拖拽到自身之下");
        }

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
            checkNotDescendant(menuCode, newUppMenuCode, menuScope, tenantId, tableName);

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
            updateChildrenLevel(menuCode, menuScope, tenantId, levelDiff, tableName, new HashSet<>());
        }
    }

    private void checkNotDescendant(String currentCode, String targetUppCode, String menuScope, String tenantId,
            String tableName) {
        String curr = targetUppCode;
        Set<String> visited = new HashSet<>();
        while (curr != null && !curr.trim().isEmpty()) {
            if (curr.equals(currentCode)) {
                throw new RuntimeException("不能将菜单拖拽到自己的子孙节点之下");
            }
            if (!visited.add(curr)) {
                break;
            }
            PermMenu p = permMenuMapper.selectByKey(curr, menuScope, tenantId, tableName);
            if (p == null) {
                break;
            }
            curr = p.getUppMenuCode();
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
                editSessionService.logSql(tableName,
                        buildDeleteSql(descendant.getMenuCode(), menuScope, tenantId, tableName));
            }
            permMenuMapper.deleteByKey(descendant.getMenuCode(), menuScope, tenantId, tableName);
        }

        if (!"perm_menu".equals(tableName)) {
            editSessionService.logSql(tableName, buildDeleteSql(menuCode, menuScope, tenantId, tableName));
        }
        permMenuMapper.deleteByKey(menuCode, menuScope, tenantId, tableName);
    }

    private void updateChildrenLevel(String parentCode, String menuScope,
            String tenantId, long levelDiff, String tableName, Set<String> visited) {
        if (!visited.add(parentCode)) {
            return;
        }
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
            updateChildrenLevel(child.getMenuCode(), menuScope, tenantId, levelDiff, tableName, visited);
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
                Comparator.nullsLast(String::compareTo)));
        for (MenuTreeNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeNodes(node.getChildren());
            }
        }
    }

    private void setDefaultValues(PermMenu menu) {
        if (menu.getTenantId() == null || menu.getTenantId().isEmpty())
            menu.setTenantId("047");
        if (menu.getStat() == null || menu.getStat().isEmpty())
            menu.setStat("1");
        if (menu.getCtrlAtti() == null || menu.getCtrlAtti().isEmpty())
            menu.setCtrlAtti("000000000100000");
        if (menu.getTbVersion() == null || menu.getTbVersion().isEmpty())
            menu.setTbVersion("3.0.0");
        if (menu.getMenuAttribute() == null || menu.getMenuAttribute().isEmpty())
            menu.setMenuAttribute("10000000");
    }

    // --- SQL Builder Methods for Audit Log ---

    private String escape(String val) {
        if (val == null)
            return "NULL";
        return "'" + val.replace("'", "''") + "'";
    }

    private String buildInsertSql(PermMenu menu, String tableName) {
        List<String> cols = new ArrayList<>(
                Arrays.asList("TENANT_ID", "STAT", "MENU_SCOPE", "MENU_CODE", "MENU_NAME", "MENU_LEVEL", "MENU_TYPE",
                        "UPP_MENU_CODE", "MENU_CHECKED", "MENU_KIND", "MENU_VERIFY", "MENU_DISPLAY", "TR_CODE"));
        List<String> vals = new ArrayList<>(Arrays.asList(escape(menu.getTenantId()), escape(menu.getStat()),
                escape(menu.getMenuScope()), escape(menu.getMenuCode()), escape(menu.getMenuName()),
                String.valueOf(menu.getMenuLevel()), escape(menu.getMenuType()), escape(menu.getUppMenuCode()),
                escape(menu.getMenuChecked()), escape(menu.getMenuKind()), escape(menu.getMenuVerify()),
                escape(menu.getMenuDisplay()), escape(menu.getTrCode())));

        if (TableMetaUtil.hasColumn(tableName, "SECURITY_TR_CODE")) {
            cols.add("SECURITY_TR_CODE");
            vals.add(escape(menu.getSecurityTrCode()));
        }
        if (TableMetaUtil.hasColumn(tableName, "CTRL_ATTI")) {
            cols.add("CTRL_ATTI");
            vals.add(escape(menu.getCtrlAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "BIZ_ATTI")) {
            cols.add("BIZ_ATTI");
            vals.add(escape(menu.getBizAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "ACCT_AUTH_ATTI")) {
            cols.add("ACCT_AUTH_ATTI");
            vals.add(escape(menu.getAcctAuthAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "ASAC_AUTH_ATTI")) {
            cols.add("ASAC_AUTH_ATTI");
            vals.add(escape(menu.getAsacAuthAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "TIME_ATTI")) {
            cols.add("TIME_ATTI");
            vals.add(escape(menu.getTimeAtti()));
        }

        cols.add("WORKFLOW_FLAG");
        vals.add(escape(menu.getWorkflowFlag()));

        if (TableMetaUtil.hasColumn(tableName, "WORKFLOW_BIZ_TYPE")) {
            cols.add("WORKFLOW_BIZ_TYPE");
            vals.add(escape(menu.getWorkflowBizType()));
        }

        cols.add("IS_ADMIN");
        vals.add(escape(menu.getIsAdmin()));
        cols.add("IS_OPERATOR");
        vals.add(escape(menu.getIsOperator()));

        if (TableMetaUtil.hasColumn(tableName, "IS_USER")) {
            cols.add("IS_USER");
            vals.add(escape(menu.getIsUser()));
        }

        cols.add("SORT_NO");
        vals.add(escape(menu.getSortNo()));
        cols.add("SUBSYSTEM_CODE");
        vals.add(escape(menu.getSubsystemCode()));
        cols.add("FOLDER_CODE");
        vals.add(escape(menu.getFolderCode()));

        if (TableMetaUtil.hasColumn(tableName, "BIZ_CATEGORY_NO")) {
            cols.add("BIZ_CATEGORY_NO");
            vals.add(escape(menu.getBizCategoryNo()));
        }
        if (TableMetaUtil.hasColumn(tableName, "BIZ_CATEGORY_NAME")) {
            cols.add("BIZ_CATEGORY_NAME");
            vals.add(escape(menu.getBizCategoryName()));
        }
        cols.add("MENU_ICON");
        vals.add(escape(menu.getMenuIcon()));
        if (TableMetaUtil.hasColumn(tableName, "MENU_HERF_TYPE")) {
            cols.add("MENU_HERF_TYPE");
            vals.add(escape(menu.getMenuHerfType()));
        }
        cols.add("MENU_HERF");
        vals.add(escape(menu.getMenuHerf()));
        cols.add("MENU_ATTRIBUTE");
        vals.add(escape(menu.getMenuAttribute()));
        cols.add("ICON_FLAG");
        vals.add(escape(menu.getIconFlag()));
        cols.add("IS_KEEP_ALIVE");
        vals.add(escape(menu.getIsKeepAlive()));
        cols.add("JUMP_HERF");
        vals.add(escape(menu.getJumpHerf()));
        if (TableMetaUtil.hasColumn(tableName, "TB_VERSION")) {
            cols.add("TB_VERSION");
            vals.add(escape(menu.getTbVersion()));
        }
        if (TableMetaUtil.hasColumn(tableName, "DESCRIPTION")) {
            cols.add("DESCRIPTION");
            vals.add(escape(menu.getDescription()));
        }

        return "INSERT INTO " + tableName + " (" + String.join(", ", cols) + ") VALUES (" + String.join(", ", vals)
                + ");";
    }

    private String buildUpdateSql(PermMenu menu, String tableName) {
        List<String> sets = new ArrayList<>();
        sets.add("MENU_NAME = " + escape(menu.getMenuName()));
        sets.add("MENU_LEVEL = " + menu.getMenuLevel());
        sets.add("MENU_TYPE = " + escape(menu.getMenuType()));
        sets.add("UPP_MENU_CODE = " + escape(menu.getUppMenuCode()));
        sets.add("MENU_CHECKED = " + escape(menu.getMenuChecked()));
        sets.add("MENU_KIND = " + escape(menu.getMenuKind()));
        sets.add("MENU_VERIFY = " + escape(menu.getMenuVerify()));
        sets.add("MENU_DISPLAY = " + escape(menu.getMenuDisplay()));
        sets.add("TR_CODE = " + escape(menu.getTrCode()));

        if (TableMetaUtil.hasColumn(tableName, "SECURITY_TR_CODE")) {
            sets.add("SECURITY_TR_CODE = " + escape(menu.getSecurityTrCode()));
        }
        if (TableMetaUtil.hasColumn(tableName, "CTRL_ATTI")) {
            sets.add("CTRL_ATTI = " + escape(menu.getCtrlAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "BIZ_ATTI")) {
            sets.add("BIZ_ATTI = " + escape(menu.getBizAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "ACCT_AUTH_ATTI")) {
            sets.add("ACCT_AUTH_ATTI = " + escape(menu.getAcctAuthAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "ASAC_AUTH_ATTI")) {
            sets.add("ASAC_AUTH_ATTI = " + escape(menu.getAsacAuthAtti()));
        }
        if (TableMetaUtil.hasColumn(tableName, "TIME_ATTI")) {
            sets.add("TIME_ATTI = " + escape(menu.getTimeAtti()));
        }
        sets.add("WORKFLOW_FLAG = " + escape(menu.getWorkflowFlag()));
        if (TableMetaUtil.hasColumn(tableName, "WORKFLOW_BIZ_TYPE")) {
            sets.add("WORKFLOW_BIZ_TYPE = " + escape(menu.getWorkflowBizType()));
        }
        sets.add("IS_ADMIN = " + escape(menu.getIsAdmin()));
        sets.add("IS_OPERATOR = " + escape(menu.getIsOperator()));
        if (TableMetaUtil.hasColumn(tableName, "IS_USER")) {
            sets.add("IS_USER = " + escape(menu.getIsUser()));
        }
        sets.add("SORT_NO = " + escape(menu.getSortNo()));
        sets.add("SUBSYSTEM_CODE = " + escape(menu.getSubsystemCode()));
        sets.add("FOLDER_CODE = " + escape(menu.getFolderCode()));
        if (TableMetaUtil.hasColumn(tableName, "BIZ_CATEGORY_NO")) {
            sets.add("BIZ_CATEGORY_NO = " + escape(menu.getBizCategoryNo()));
        }
        if (TableMetaUtil.hasColumn(tableName, "BIZ_CATEGORY_NAME")) {
            sets.add("BIZ_CATEGORY_NAME = " + escape(menu.getBizCategoryName()));
        }
        sets.add("MENU_ICON = " + escape(menu.getMenuIcon()));
        if (TableMetaUtil.hasColumn(tableName, "MENU_HERF_TYPE")) {
            sets.add("MENU_HERF_TYPE = " + escape(menu.getMenuHerfType()));
        }
        sets.add("MENU_HERF = " + escape(menu.getMenuHerf()));
        sets.add("MENU_ATTRIBUTE = " + escape(menu.getMenuAttribute()));
        sets.add("ICON_FLAG = " + escape(menu.getIconFlag()));
        sets.add("IS_KEEP_ALIVE = " + escape(menu.getIsKeepAlive()));
        sets.add("JUMP_HERF = " + escape(menu.getJumpHerf()));
        sets.add("STAT = " + escape(menu.getStat()));
        if (TableMetaUtil.hasColumn(tableName, "TB_VERSION")) {
            sets.add("TB_VERSION = " + escape(menu.getTbVersion()));
        }
        if (TableMetaUtil.hasColumn(tableName, "DESCRIPTION")) {
            sets.add("DESCRIPTION = " + escape(menu.getDescription()));
        }

        return "UPDATE " + tableName + " SET " + String.join(", ", sets) + " WHERE MENU_CODE = "
                + escape(menu.getMenuCode()) + " AND MENU_SCOPE = " + escape(menu.getMenuScope()) + " AND TENANT_ID = "
                + escape(menu.getTenantId()) + ";";
    }

    private String buildDeleteSql(String menuCode, String menuScope, String tenantId, String tableName) {
        return String.format(
                "DELETE FROM %s WHERE MENU_CODE = %s AND MENU_SCOPE = %s AND TENANT_ID = %s;",
                tableName, escape(menuCode), escape(menuScope), escape(tenantId));
    }

    private String buildUpdateUppMenuCodeSql(String oldCode, String newCode, String menuScope, String tenantId,
            String tableName) {
        return String.format(
                "UPDATE %s SET UPP_MENU_CODE = %s WHERE UPP_MENU_CODE = %s AND MENU_SCOPE = %s AND TENANT_ID = %s;",
                tableName, escape(newCode), escape(oldCode), escape(menuScope), escape(tenantId));
    }

    @Override
    public List<MenuFeatureMountVO> getFeatureMounts(String menuScope, String menuCode, String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "047";
        }
        try {
            return permFeatureMenuMapper.selectFeatureMountsByMenu(menuScope, menuCode, tenantId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProdFeatureVO> getProdFeatures(String tenantId, String keyword) {
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "047";
        }
        try {
            return permProdFeatureMapper.selectProdFeatures(tenantId, keyword);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFeatureMount(PermFeatureMenu request) {
        if (request.getTenantId() == null || request.getTenantId().isEmpty()) {
            request.setTenantId("047");
        }
        if (request.getStat() == null || request.getStat().isEmpty()) {
            request.setStat("1");
        }
        int existCount = permFeatureMenuMapper.checkExistFeatureMenu(
                request.getMenuScope(),
                request.getMenuCode(),
                request.getProdCode(),
                request.getFeatureId(),
                request.getTenantId());
        if (existCount > 0) {
            throw new RuntimeException("该菜单已经加挂到该产品功能，请勿重复加挂");
        }
        permFeatureMenuMapper.insertFeatureMenu(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeatureMount(String menuScope, String menuCode, String prodCode, String featureId, String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "047";
        }
        permFeatureMenuMapper.deleteFeatureMenu(menuScope, menuCode, prodCode, featureId, tenantId);
    }
}
