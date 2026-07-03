package com.example.permmenu.controller;

import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.EnvConfig;
import com.example.permmenu.service.EnvConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/env")
public class EnvConfigController {

    @Autowired
    private EnvConfigService envConfigService;

    @GetMapping("/list")
    public ResultVO<List<EnvConfig>> list() {
        return ResultVO.success(envConfigService.getAllEnvs());
    }

    @PostMapping("/add")
    public ResultVO<Void> add(@RequestBody EnvConfig config) {
        try {
            envConfigService.addEnv(config);
            return ResultVO.success(null);
        } catch (Exception e) {
            return ResultVO.error(500, e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResultVO<Void> update(@RequestBody EnvConfig config) {
        try {
            envConfigService.updateEnv(config);
            return ResultVO.success(null);
        } catch (Exception e) {
            return ResultVO.error(500, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultVO<Void> delete(@RequestParam String envId) {
        try {
            envConfigService.deleteEnv(envId);
            return ResultVO.success(null);
        } catch (Exception e) {
            return ResultVO.error(500, e.getMessage());
        }
    }
}
