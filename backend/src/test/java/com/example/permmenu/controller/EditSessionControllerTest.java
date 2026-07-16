package com.example.permmenu.controller;

import com.example.permmenu.entity.EditLock;
import com.example.permmenu.service.EditSessionService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class EditSessionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EditSessionService editSessionService;

    @InjectMocks
    private EditSessionController editSessionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(editSessionController).build();
    }

    @Test
    public void testGetStatusLocked() throws Exception {
        EditLock lock = new EditLock();
        lock.setLockedBy("tester");
        lock.setLockedAt(LocalDateTime.now());
        lock.setTempTableName("perm_menu_1001");
        lock.setSubsystemCode("SYS");
        when(editSessionService.getActiveLock()).thenReturn(lock);

        mockMvc.perform(get("/session/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isLocked").value(true))
                .andExpect(jsonPath("$.data.lockedBy").value("tester"))
                .andExpect(jsonPath("$.data.tempTableName").value("perm_menu_1001"));
    }

    @Test
    public void testGetStatusUnlocked() throws Exception {
        when(editSessionService.getActiveLock()).thenReturn(null);

        mockMvc.perform(get("/session/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isLocked").value(false));
    }

    @Test
    public void testUnlockSessionSuccess() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("lockedBy", "tester");
        req.put("subsystemCode", "SYS");
        when(editSessionService.unlockSession("tester", "SYS")).thenReturn("perm_menu_2002");

        mockMvc.perform(post("/session/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tempTableName").value("perm_menu_2002"));
    }

    @Test
    public void testUnlockSessionMissingLockedBy() throws Exception {
        Map<String, String> req = new HashMap<>();
        mockMvc.perform(post("/session/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("缺少 lockedBy 参数"));
    }

    @Test
    public void testUnlockSessionConflict() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("lockedBy", "tester");
        when(editSessionService.unlockSession(eq("tester"), any())).thenThrow(new RuntimeException("会话已被占用"));

        mockMvc.perform(post("/session/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("会话已被占用"));
    }

    @Test
    public void testGetSqlLog() throws Exception {
        when(editSessionService.getSqlLog("perm_menu_1001")).thenReturn(Collections.singletonList("INSERT INTO ..."));

        mockMvc.perform(get("/session/sql-log").param("tempTableName", "perm_menu_1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("INSERT INTO ..."));
    }

    @Test
    public void testCommitSessionSuccess() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("tempTableName", "perm_menu_1001");
        Map<String, Object> res = new HashMap<>();
        res.put("status", "COMMITTED");
        when(editSessionService.commitSession("perm_menu_1001")).thenReturn(res);

        mockMvc.perform(post("/session/commit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMMITTED"));
    }

    @Test
    public void testDropTempTableSuccess() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("tempTableName", "perm_menu_1001");

        mockMvc.perform(post("/session/drop-temp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(editSessionService, times(1)).dropTempTable("perm_menu_1001");
    }

    @Test
    public void testCancelSessionSuccess() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("tempTableName", "perm_menu_1001");

        mockMvc.perform(post("/session/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(editSessionService, times(1)).cancelSession("perm_menu_1001");
    }
}
