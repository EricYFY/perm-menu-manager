package com.example.permmenu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.ItspFlowUmpConfig;
import com.example.permmenu.service.ItspFlowUmpConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/flow-ump-config")
public class ItspFlowUmpConfigController {

    @Autowired
    private ItspFlowUmpConfigService itspFlowUmpConfigService;

    @GetMapping("/page")
    public ResultVO<?> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String trCode,
            @RequestParam(required = false) String trName,
            @RequestParam(required = false) String trDesc) {
        LambdaQueryWrapper<ItspFlowUmpConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(trCode)) {
            wrapper.like(ItspFlowUmpConfig::getTrCode, trCode);
        }
        if (StringUtils.hasText(trName)) {
            wrapper.like(ItspFlowUmpConfig::getTrName, trName);
        }
        if (StringUtils.hasText(trDesc)) {
            wrapper.like(ItspFlowUmpConfig::getTrDesc, trDesc);
        }
        
        Page<ItspFlowUmpConfig> page = itspFlowUmpConfigService.page(new Page<>(current, size), wrapper);
        return ResultVO.success(page);
    }

    @PostMapping
    public ResultVO<Boolean> save(@RequestBody ItspFlowUmpConfig itspFlowUmpConfig) {
        if (!StringUtils.hasText(itspFlowUmpConfig.getCfgId())) {
            itspFlowUmpConfig.setCfgId(UUID.randomUUID().toString().replace("-", ""));
        }
        return ResultVO.success(itspFlowUmpConfigService.save(itspFlowUmpConfig));
    }

    @PostMapping("/update")
    public ResultVO<Boolean> update(@RequestBody ItspFlowUmpConfig itspFlowUmpConfig) {
        return ResultVO.success(itspFlowUmpConfigService.updateById(itspFlowUmpConfig));
    }

    @PostMapping("/delete/{id}")
    public ResultVO<Boolean> delete(@PathVariable String id) {
        return ResultVO.success(itspFlowUmpConfigService.removeById(id));
    }
}
