package com.example.permmenu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.permmenu.entity.ItspFlowUmpConfig;
import com.example.permmenu.service.ItspFlowUmpConfigService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItspFlowUmpConfigControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItspFlowUmpConfigService itspFlowUmpConfigService;

    @InjectMocks
    private ItspFlowUmpConfigController itspFlowUmpConfigController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(itspFlowUmpConfigController).build();
    }

    @Test
    public void testPageSuccess() throws Exception {
        Page<ItspFlowUmpConfig> page = new Page<>(1, 10);
        ItspFlowUmpConfig config = new ItspFlowUmpConfig();
        config.setTrCode("TR01");
        page.setRecords(Collections.singletonList(config));
        when(itspFlowUmpConfigService.page(any(), any())).thenReturn(page);

        mockMvc.perform(get("/flow-ump-config/page")
                        .param("current", "1")
                        .param("size", "10")
                        .param("trCode", "TR01")
                        .param("trName", "测试")
                        .param("trDesc", "描述"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].trCode").value("TR01"));
    }

    @Test
    public void testSaveSuccess() throws Exception {
        ItspFlowUmpConfig config = new ItspFlowUmpConfig();
        config.setTrCode("TR01");
        when(itspFlowUmpConfigService.save(any(ItspFlowUmpConfig.class))).thenReturn(true);

        mockMvc.perform(post("/flow-ump-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        ItspFlowUmpConfig config = new ItspFlowUmpConfig();
        config.setCfgId("CFG001");
        when(itspFlowUmpConfigService.updateById(any(ItspFlowUmpConfig.class))).thenReturn(true);

        mockMvc.perform(post("/flow-ump-config/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        when(itspFlowUmpConfigService.removeById("CFG001")).thenReturn(true);

        mockMvc.perform(post("/flow-ump-config/delete/CFG001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}
