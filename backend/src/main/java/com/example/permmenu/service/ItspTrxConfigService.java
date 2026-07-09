package com.example.permmenu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permmenu.entity.ItspTrxConfig;

import java.util.List;

public interface ItspTrxConfigService extends IService<ItspTrxConfig> {

    List<ItspTrxConfig> listByCondition(String trCode, String productName, String busiName, String trName);

    void addConfig(ItspTrxConfig config);

    void updateConfig(ItspTrxConfig config);

    void deleteConfig(String trCode, String language);
}
