package com.example.permmenu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permmenu.entity.ItspTrxConfig;
import com.example.permmenu.mapper.ItspTrxConfigMapper;
import com.example.permmenu.service.ItspTrxConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ItspTrxConfigServiceImpl extends ServiceImpl<ItspTrxConfigMapper, ItspTrxConfig> implements ItspTrxConfigService {

    @Override
    public List<ItspTrxConfig> listByCondition(String trCode, String productName, String busiName, String trName) {
        QueryWrapper<ItspTrxConfig> queryWrapper = new QueryWrapper<>();
        if (trCode != null && !trCode.trim().isEmpty()) {
            queryWrapper.eq("TR_CODE", trCode.trim());
        }
        if (productName != null && !productName.trim().isEmpty()) {
            queryWrapper.like("PRODUCT_NAME", productName.trim());
        }
        if (busiName != null && !busiName.trim().isEmpty()) {
            queryWrapper.like("BUSI_NAME", busiName.trim());
        }
        if (trName != null && !trName.trim().isEmpty()) {
            queryWrapper.like("TR_NAME", trName.trim());
        }
        log.info("【多语言交易配置查询】过滤条件: trCode={}, productName={}, busiName={}, trName={}", trCode, productName, busiName, trName);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public void addConfig(ItspTrxConfig config) {
        log.info("【新增多语言交易配置】TR_CODE: [{}], LANGUAGE: [{}], TR_NAME: [{}]", config.getTrCode(), config.getLanguage(), config.getTrName());
        QueryWrapper<ItspTrxConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("TR_CODE", config.getTrCode())
                    .eq("LANGUAGE", config.getLanguage());
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("配置已存在: TR_CODE=" + config.getTrCode() + ", LANGUAGE=" + config.getLanguage());
        }
        this.save(config);
    }

    @Override
    @Transactional
    public void updateConfig(ItspTrxConfig config) {
        log.info("【更新多语言交易配置】TR_CODE: [{}], LANGUAGE: [{}]", config.getTrCode(), config.getLanguage());
        baseMapper.updateByKeys(config);
    }

    @Override
    @Transactional
    public void deleteConfig(String trCode, String language) {
        log.info("【删除多语言交易配置】TR_CODE: [{}], LANGUAGE: [{}]", trCode, language);
        baseMapper.deleteByKeys(trCode, language);
    }
}
