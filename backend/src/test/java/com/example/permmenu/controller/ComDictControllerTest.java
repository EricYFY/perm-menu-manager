package com.example.permmenu.controller;

import com.example.permmenu.entity.ComDict;
import com.example.permmenu.service.ComDictService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ComDictControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComDictService comDictService;

    @InjectMocks
    private ComDictController comDictController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(comDictController).build();
    }

    @Test
    public void testListDictIdsSuccess() throws Exception {
        ComDict dict = new ComDict();
        dict.setDictId("STATUS");
        when(comDictService.listDictIds("047")).thenReturn(Collections.singletonList(dict));

        mockMvc.perform(get("/dict/ids").param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].dictId").value("STATUS"));
    }

    @Test
    public void testListDictIdsError() throws Exception {
        when(comDictService.listDictIds("047")).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/dict/ids").param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询字典ID列表失败：DB Error"));
    }

    @Test
    public void testListByDictIdSuccess() throws Exception {
        ComDict dict = new ComDict();
        dict.setDictId("STATUS");
        dict.setDictKey("1");
        dict.setDictValue("正常");
        when(comDictService.listByDictId("STATUS", "047")).thenReturn(Collections.singletonList(dict));

        mockMvc.perform(get("/dict/STATUS").param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].dictValue").value("正常"));
    }

    @Test
    public void testAddDictEntrySuccess() throws Exception {
        ComDict dict = new ComDict();
        dict.setDictId("STATUS");
        dict.setDictKey("1");

        mockMvc.perform(post("/dict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dict)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(comDictService, times(1)).addDictEntry(any(ComDict.class));
    }

    @Test
    public void testUpdateDictEntrySuccess() throws Exception {
        ComDict dict = new ComDict();
        dict.setDictId("STATUS");
        dict.setDictKey("1");

        mockMvc.perform(post("/dict/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dict)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(comDictService, times(1)).updateDictEntry(any(ComDict.class));
    }

    @Test
    public void testDeleteDictEntrySuccess() throws Exception {
        mockMvc.perform(post("/dict/delete/STATUS/1").param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(comDictService, times(1)).deleteDictEntry("STATUS", "1", "047");
    }

    @Test
    public void testDeleteDictGroupSuccess() throws Exception {
        mockMvc.perform(post("/dict/delete/STATUS").param("tenantId", "047"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(comDictService, times(1)).deleteDictGroup("STATUS", "047");
    }
}
