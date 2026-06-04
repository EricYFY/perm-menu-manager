package com.example.permmenu.controller;

import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.EditLock;
import com.example.permmenu.service.EditSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 编辑会话管理控制器
 */
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class EditSessionController {

    private final EditSessionService editSessionService;

    /**
     * 获取当前锁状态
     */
    @GetMapping("/status")
    public ResultVO<Map<String, Object>> getStatus() {
        EditLock lock = editSessionService.getActiveLock();
        Map<String, Object> result = new HashMap<>();
        if (lock != null) {
            result.put("isLocked", true);
            result.put("lockedBy", lock.getLockedBy());
            result.put("lockedAt", lock.getLockedAt());
            result.put("tempTableName", lock.getTempTableName());
            result.put("subsystemCode", lock.getSubsystemCode());
        } else {
            result.put("isLocked", false);
        }
        return ResultVO.success(result);
    }

    /**
     * 解锁进入编辑模式
     *
     * @param request 包含 lockedBy
     */
    @PostMapping("/unlock")
    public ResultVO<Map<String, String>> unlockSession(@RequestBody Map<String, String> request) {
        try {
            String lockedBy = request.get("lockedBy");
            String subsystemCode = request.get("subsystemCode");
            if (lockedBy == null || lockedBy.trim().isEmpty()) {
                return ResultVO.error("缺少 lockedBy 参数");
            }
            String tempTableName = editSessionService.unlockSession(lockedBy, subsystemCode);
            Map<String, String> result = new HashMap<>();
            result.put("tempTableName", tempTableName);
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error(409, e.getMessage()); // 409 Conflict
        }
    }

    /**
     * 获取 SQL 日志列表
     */
    @GetMapping("/sql-log")
    public ResultVO<List<String>> getSqlLog(@RequestParam String tempTableName) {
        List<String> logs = editSessionService.getSqlLog(tempTableName);
        return ResultVO.success(logs);
    }

    /**
     * 确认提交（回放 SQL）
     */
    @PostMapping("/commit")
    public ResultVO<Map<String, Object>> commitSession(@RequestBody Map<String, String> request) {
        try {
            String tempTableName = request.get("tempTableName");
            Map<String, Object> result = editSessionService.commitSession(tempTableName);
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 删除临时表
     */
    @PostMapping("/drop-temp")
    public ResultVO<Void> dropTempTable(@RequestBody Map<String, String> request) {
        try {
            String tempTableName = request.get("tempTableName");
            editSessionService.dropTempTable(tempTableName);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("删除临时表失败：" + e.getMessage());
        }
    }

    /**
     * 取消编辑会话
     */
    @PostMapping("/cancel")
    public ResultVO<Void> cancelSession(@RequestBody Map<String, String> request) {
        try {
            String tempTableName = request.get("tempTableName");
            editSessionService.cancelSession(tempTableName);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("取消会话失败：" + e.getMessage());
        }
    }
}
