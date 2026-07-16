package com.example.permmenu.controller;

import com.example.permmenu.entity.EnvConfig;
import com.example.permmenu.service.EnvConfigService;
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
public class EnvConfigControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnvConfigService envConfigService;

    @InjectMocks
    private EnvConfigController envConfigController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(envConfigController).build();
    }

    @Test
    public void testListSuccess() throws Exception {
        EnvConfig config = new EnvConfig();
        config.setEnvId("SIT");
        config.setEnvName("测试环境");
        when(envConfigService.getAllEnvs()).thenReturn(Collections.singletonList(config));

        mockMvc.perform(get("/env/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].envId").value("SIT"));
    }

    @Test
    public void testAddSuccess() throws Exception {
        EnvConfig config = new EnvConfig();
        config.setEnvId("SIT");

        mockMvc.perform(post("/env/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(envConfigService, times(1)).addEnv(any(EnvConfig.class));
    }

    @Test
    public void testAddError() throws Exception {
        EnvConfig config = new EnvConfig();
        doThrow(new RuntimeException("环境已经存在")).when(envConfigService).addEnv(any(EnvConfig.class));

        mockMvc.perform(post("/env/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("环境已经存在"));
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        EnvConfig config = new EnvConfig();
        config.setEnvId("SIT");

        mockMvc.perform(post("/env/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(envConfigService, times(1)).updateEnv(any(EnvConfig.class));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        mockMvc.perform(post("/env/delete").param("envId", "SIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(envConfigService, times(1)).deleteEnv("SIT");
    }
}
