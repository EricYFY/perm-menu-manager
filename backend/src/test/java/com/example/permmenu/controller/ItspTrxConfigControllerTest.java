package com.example.permmenu.controller;

import com.example.permmenu.entity.ItspTrxConfig;
import com.example.permmenu.service.ItspTrxConfigService;
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
public class ItspTrxConfigControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItspTrxConfigService itspTrxConfigService;

    @InjectMocks
    private ItspTrxConfigController itspTrxConfigController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(itspTrxConfigController).build();
    }

    @Test
    public void testListSuccess() throws Exception {
        ItspTrxConfig config = new ItspTrxConfig();
        config.setTrCode("TR01");
        when(itspTrxConfigService.listByCondition("TR01", "P", "B", "T")).thenReturn(Collections.singletonList(config));

        mockMvc.perform(get("/trx-config")
                        .param("trCode", "TR01")
                        .param("productName", "P")
                        .param("busiName", "B")
                        .param("trName", "T"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].trCode").value("TR01"));
    }

    @Test
    public void testAddSuccess() throws Exception {
        ItspTrxConfig config = new ItspTrxConfig();
        config.setTrCode("TR01");

        mockMvc.perform(post("/trx-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(itspTrxConfigService, times(1)).addConfig(any(ItspTrxConfig.class));
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        ItspTrxConfig config = new ItspTrxConfig();
        config.setTrCode("TR01");

        mockMvc.perform(post("/trx-config/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(itspTrxConfigService, times(1)).updateConfig(any(ItspTrxConfig.class));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        mockMvc.perform(post("/trx-config/delete/TR01/ZH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(itspTrxConfigService, times(1)).deleteConfig("TR01", "ZH");
    }
}
