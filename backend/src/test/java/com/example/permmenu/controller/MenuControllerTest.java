package com.example.permmenu.controller;

import com.example.permmenu.dto.*;
import com.example.permmenu.entity.PermFeatureMenu;
import com.example.permmenu.entity.PermMenu;
import com.example.permmenu.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MenuControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();
    }

    @Test
    public void testGetMenuTreeSuccess() throws Exception {
        MenuTreeNode node = new MenuTreeNode();
        node.setMenuCode("M01");
        node.setMenuName("系统管理");
        when(menuService.getMenuTree(eq("11"), eq("047"), eq("perm_menu"), isNull()))
                .thenReturn(Collections.singletonList(node));

        mockMvc.perform(get("/menu/tree")
                        .param("menuScope", "11")
                        .param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].menuCode").value("M01"));
    }

    @Test
    public void testGetMenuTreeWithTempTable() throws Exception {
        MenuTreeNode node = new MenuTreeNode();
        node.setMenuCode("M01");
        when(menuService.getMenuTree(eq("11"), eq("047"), eq("perm_menu_temp"), eq("SYS")))
                .thenReturn(Collections.singletonList(node));

        mockMvc.perform(get("/menu/tree")
                        .param("menuScope", "11")
                        .param("tenantId", "047")
                        .param("subsystemCode", "SYS")
                        .header("X-Temp-Table", "perm_menu_temp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].menuCode").value("M01"));
    }

    @Test
    public void testAddMenuSuccess() throws Exception {
        PermMenu menu = new PermMenu();
        menu.setMenuCode("M01");

        mockMvc.perform(post("/menu/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menu))
                        .header("X-Temp-Table", "perm_menu_temp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).addMenu(any(PermMenu.class), eq("perm_menu_temp"));
    }

    @Test
    public void testUpdateMenuSuccess() throws Exception {
        PermMenu menu = new PermMenu();
        menu.setMenuCode("M01");

        mockMvc.perform(post("/menu/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menu)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).updateMenu(any(PermMenu.class), isNull());
    }

    @Test
    public void testUpdateMenuCodeSuccess() throws Exception {
        MenuCodeUpdateRequest req = new MenuCodeUpdateRequest();
        req.setOldMenuCode("OLD");
        req.setNewMenuCode("NEW");

        mockMvc.perform(post("/menu/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).updateMenuCode(any(MenuCodeUpdateRequest.class), isNull());
    }

    @Test
    public void testDragMenuSuccess() throws Exception {
        MenuDragRequest req = new MenuDragRequest();
        req.setMenuCode("M01");
        req.setNewUppMenuCode("ROOT");

        mockMvc.perform(post("/menu/drag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).dragMenu(any(MenuDragRequest.class), isNull());
    }

    @Test
    public void testDeleteMenuSuccess() throws Exception {
        mockMvc.perform(post("/menu/delete/11/M01").param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).deleteMenu("M01", "11", "047", null);
    }

    @Test
    public void testGetFeatureMountsSuccess() throws Exception {
        MenuFeatureMountVO vo = new MenuFeatureMountVO();
        vo.setMenuScope("11");
        vo.setMenuCode("M01");
        vo.setProdCode("PROD1");
        vo.setProdName("测试产品");
        vo.setFeatureId("F01");
        vo.setFeatureName("测试功能");

        when(menuService.getFeatureMounts("11", "M01", "047")).thenReturn(Collections.singletonList(vo));

        mockMvc.perform(get("/menu/feature-mounts")
                        .param("menuScope", "11")
                        .param("menuCode", "M01")
                        .param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].prodCode").value("PROD1"))
                .andExpect(jsonPath("$.data[0].featureName").value("测试功能"));

        verify(menuService, times(1)).getFeatureMounts("11", "M01", "047");
    }

    @Test
    public void testGetProdFeaturesSuccess() throws Exception {
        ProdFeatureVO vo = new ProdFeatureVO();
        vo.setTenantId("047");
        vo.setProdCode("PROD1");
        vo.setProdName("测试产品");
        vo.setFeatureId("F01");
        vo.setFeatureName("测试功能");

        when(menuService.getProdFeatures("047", "测试")).thenReturn(Collections.singletonList(vo));

        mockMvc.perform(get("/menu/prod-features")
                        .param("tenantId", "047")
                        .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].prodCode").value("PROD1"))
                .andExpect(jsonPath("$.data[0].featureName").value("测试功能"));

        verify(menuService, times(1)).getProdFeatures("047", "测试");
    }

    @Test
    public void testAddFeatureMountSuccess() throws Exception {
        PermFeatureMenu req = new PermFeatureMenu();
        req.setMenuScope("11");
        req.setMenuCode("M01");
        req.setProdCode("PROD1");
        req.setFeatureId("F01");
        req.setTenantId("047");

        mockMvc.perform(post("/menu/feature-mount/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).addFeatureMount(any(PermFeatureMenu.class));
    }

    @Test
    public void testDeleteFeatureMountSuccess() throws Exception {
        mockMvc.perform(post("/menu/feature-mount/delete")
                        .param("menuScope", "11")
                        .param("menuCode", "M01")
                        .param("prodCode", "PROD1")
                        .param("featureId", "F01")
                        .param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuService, times(1)).deleteFeatureMount("11", "M01", "PROD1", "F01", "047");
    }
}
