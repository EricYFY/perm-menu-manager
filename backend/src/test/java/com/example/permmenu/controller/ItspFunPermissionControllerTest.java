package com.example.permmenu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.permmenu.dto.FunPermissionGroupVO;
import com.example.permmenu.entity.ItspFunPermission;
import com.example.permmenu.mapper.ItspFunPermissionMapper;
import com.example.permmenu.service.ItspFunPermissionService;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItspFunPermissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItspFunPermissionService itspFunPermissionService;

    @Mock
    private ItspFunPermissionMapper itspFunPermissionMapper;

    @InjectMocks
    private ItspFunPermissionController itspFunPermissionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(itspFunPermissionController).build();
    }

    @Test
    public void testGetOptionsSuccess() throws Exception {
        Map<String, String> option = new HashMap<>();
        option.put("value", "MOD");
        option.put("label", "模块");
        when(itspFunPermissionMapper.getDistinctOptions("trans_module", "trans_module_desc"))
                .thenReturn(Collections.singletonList(option));

        mockMvc.perform(get("/fun-permission/options/transModule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].value").value("MOD"));
    }

    @Test
    public void testGetOptionsInvalidField() throws Exception {
        mockMvc.perform(get("/fun-permission/options/invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Invalid field"));
    }

    @Test
    public void testGroupPageSuccess() throws Exception {
        Page<FunPermissionGroupVO> page = new Page<>(1, 10);
        FunPermissionGroupVO vo = new FunPermissionGroupVO();
        vo.setTransModule("MOD");
        page.setRecords(Collections.singletonList(vo));
        when(itspFunPermissionMapper.selectGroupPage(any(), any())).thenReturn(page);

        mockMvc.perform(get("/fun-permission/group-page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("transModule", "MOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].transModule").value("MOD"));
    }

    @Test
    public void testPageSuccess() throws Exception {
        Page<ItspFunPermission> page = new Page<>(1, 10);
        ItspFunPermission perm = new ItspFunPermission();
        perm.setTransModule("MOD");
        page.setRecords(Collections.singletonList(perm));
        when(itspFunPermissionService.page(any(), any())).thenReturn(page);

        mockMvc.perform(get("/fun-permission/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("transModule", "MOD")
                        .param("busiType", "BUSI")
                        .param("userRole", "ROLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].transModule").value("MOD"));
    }

    @Test
    public void testSaveSuccess() throws Exception {
        ItspFunPermission perm = new ItspFunPermission();
        perm.setTransModule("MOD");
        when(itspFunPermissionService.save(any(ItspFunPermission.class))).thenReturn(true);

        mockMvc.perform(post("/fun-permission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(perm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void testSaveBatchSuccess() throws Exception {
        ItspFunPermission perm = new ItspFunPermission();
        perm.setTransModule("MOD");
        when(itspFunPermissionService.saveBatch(anyList())).thenReturn(true);

        mockMvc.perform(post("/fun-permission/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(perm))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        ItspFunPermission perm = new ItspFunPermission();
        perm.setPermissionId("PID");
        when(itspFunPermissionService.updateById(any(ItspFunPermission.class))).thenReturn(true);

        mockMvc.perform(post("/fun-permission/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(perm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        when(itspFunPermissionService.removeById("PID")).thenReturn(true);

        mockMvc.perform(post("/fun-permission/delete/PID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}
