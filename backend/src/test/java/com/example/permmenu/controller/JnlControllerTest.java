package com.example.permmenu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.permmenu.entity.TbspData;
import com.example.permmenu.entity.TbspJnl;
import com.example.permmenu.service.JnlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class JnlControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JnlService jnlService;

    @InjectMocks
    private JnlController jnlController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jnlController).build();
    }

    @Test
    public void testGetJnlPageSuccess() throws Exception {
        IPage<TbspJnl> page = new Page<>(1, 20);
        TbspJnl jnl = new TbspJnl();
        jnl.setTrCode("TR01");
        page.setRecords(Collections.singletonList(jnl));
        when(jnlService.getJnlPage("jnl", "TR01", "C01", null, null, 1, 20)).thenReturn(page);

        mockMvc.perform(get("/jnl/page")
                        .param("type", "jnl")
                        .param("trCode", "TR01")
                        .param("custNo", "C01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].trCode").value("TR01"));
    }

    @Test
    public void testGetJnlPageIllegalArg() throws Exception {
        when(jnlService.getJnlPage(anyString(), anyString(), anyString(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("参数不合法"));

        mockMvc.perform(get("/jnl/page")
                        .param("type", "invalid")
                        .param("trCode", "TR01")
                        .param("custNo", "C01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数不合法"));
    }

    @Test
    public void testGetJnlDataSuccess() throws Exception {
        TbspData data = new TbspData();
        data.setRequestContext("<req/>");
        when(jnlService.getJnlData("C01", "S01")).thenReturn(data);

        mockMvc.perform(get("/jnl/data")
                        .param("custNo", "C01")
                        .param("serialNo", "S01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requestContext").value("<req/>"));
    }

    @Test
    public void testGetJnlDataException() throws Exception {
        when(jnlService.getJnlData("C01", "S01")).thenThrow(new RuntimeException("查询超时"));

        mockMvc.perform(get("/jnl/data")
                        .param("custNo", "C01")
                        .param("serialNo", "S01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询报文异常: 查询超时"));
    }

    @Test
    public void testCheckSuccessTrue() throws Exception {
        when(jnlService.checkSuccess("TR01", Arrays.asList("C01", "C02"), "CH")).thenReturn(true);

        mockMvc.perform(get("/jnl/check-success")
                        .param("trCode", "TR01")
                        .param("custNos", "C01,C02")
                        .param("channelNo", "CH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}
