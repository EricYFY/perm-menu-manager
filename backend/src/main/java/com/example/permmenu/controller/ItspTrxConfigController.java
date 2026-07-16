package com.example.permmenu.controller;

import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.ItspTrxConfig;
import com.example.permmenu.service.ItspTrxConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trx-config")
public class ItspTrxConfigController {

    @Autowired
    private ItspTrxConfigService itspTrxConfigService;

    @GetMapping
    public ResultVO<List<ItspTrxConfig>> list(
            @RequestParam(required = false) String trCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String busiName,
            @RequestParam(required = false) String trName) {
        return ResultVO.success(itspTrxConfigService.listByCondition(trCode, productName, busiName, trName));
    }

    @PostMapping
    public ResultVO<Void> add(@RequestBody ItspTrxConfig config) {
        itspTrxConfigService.addConfig(config);
        return ResultVO.success();
    }

    @PostMapping("/update")
    public ResultVO<Void> update(@RequestBody ItspTrxConfig config) {
        itspTrxConfigService.updateConfig(config);
        return ResultVO.success();
    }

    @PostMapping("/delete/{trCode}/{language}")
    public ResultVO<Void> delete(@PathVariable String trCode, @PathVariable String language) {
        itspTrxConfigService.deleteConfig(trCode, language);
        return ResultVO.success();
    }
}
