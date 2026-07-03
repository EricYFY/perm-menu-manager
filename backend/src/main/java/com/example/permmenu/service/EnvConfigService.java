package com.example.permmenu.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.example.permmenu.entity.EnvConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EnvConfigService {

    private static final String ENVS_FILE_PATH = "envs.json";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DefaultDataSourceCreator dataSourceCreator;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<EnvConfig> envList = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadFromFile();
        for (EnvConfig config : envList) {
            registerDatasource(config);
        }
    }

    private synchronized void loadFromFile() {
        File file = new File(ENVS_FILE_PATH);
        if (file.exists()) {
            try {
                envList = objectMapper.readValue(file, new TypeReference<List<EnvConfig>>() {});
                log.info("Loaded {} environments from {}", envList.size(), ENVS_FILE_PATH);
            } catch (IOException e) {
                log.error("Failed to read envs.json", e);
                envList = new ArrayList<>();
            }
        }
    }

    private synchronized void saveToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(ENVS_FILE_PATH), envList);
        } catch (IOException e) {
            log.error("Failed to write envs.json", e);
            throw new RuntimeException("无法保存环境配置到文件", e);
        }
    }

    public List<EnvConfig> getAllEnvs() {
        return envList;
    }

    public void addEnv(EnvConfig config) {
        if (envList.stream().anyMatch(e -> e.getEnvId().equals(config.getEnvId()))) {
            throw new RuntimeException("环境ID已存在: " + config.getEnvId());
        }
        envList.add(config);
        saveToFile();
        registerDatasource(config);
    }

    public void updateEnv(EnvConfig config) {
        Optional<EnvConfig> opt = envList.stream().filter(e -> e.getEnvId().equals(config.getEnvId())).findFirst();
        if (!opt.isPresent()) {
            throw new RuntimeException("环境ID不存在: " + config.getEnvId());
        }
        EnvConfig existing = opt.get();
        envList.remove(existing);
        envList.add(config);
        saveToFile();
        // 如果运行时修改，需要先移除旧的再添加新的
        removeDatasource(config.getEnvId());
        registerDatasource(config);
    }

    public void deleteEnv(String envId) {
        boolean removed = envList.removeIf(e -> e.getEnvId().equals(envId));
        if (removed) {
            saveToFile();
            removeDatasource(envId);
        }
    }

    private void registerDatasource(EnvConfig config) {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            log.warn("Current datasource is not DynamicRoutingDataSource, skip dynamic registry.");
            return;
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;

        try {
            // Master 库
            if (config.getMasterUrl() != null && !config.getMasterUrl().isEmpty()) {
                DataSourceProperty masterProp = new DataSourceProperty();
                masterProp.setPoolName(config.getEnvId() + "_master");
                masterProp.setUrl(config.getMasterUrl());
                masterProp.setUsername(config.getMasterUsername());
                masterProp.setPassword(config.getMasterPassword());
                masterProp.setDriverClassName(config.getMasterDriver());
                DataSource masterDs = dataSourceCreator.createDataSource(masterProp);
                drds.addDataSource(config.getEnvId() + "_master", masterDs);
                log.info("Registered datasource: {}_master", config.getEnvId());
            }

            // Second 库
            if (config.getSecondUrl() != null && !config.getSecondUrl().isEmpty()) {
                DataSourceProperty secondProp = new DataSourceProperty();
                secondProp.setPoolName(config.getEnvId() + "_second");
                secondProp.setUrl(config.getSecondUrl());
                secondProp.setUsername(config.getSecondUsername());
                secondProp.setPassword(config.getSecondPassword());
                secondProp.setDriverClassName(config.getSecondDriver());
                DataSource secondDs = dataSourceCreator.createDataSource(secondProp);
                drds.addDataSource(config.getEnvId() + "_second", secondDs);
                log.info("Registered datasource: {}_second", config.getEnvId());
            }
        } catch (Exception e) {
            log.error("Failed to register datasource for env: " + config.getEnvId(), e);
            throw new RuntimeException("注册数据源失败: " + e.getMessage());
        }
    }

    private void removeDatasource(String envId) {
        if (dataSource instanceof DynamicRoutingDataSource) {
            DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
            drds.removeDataSource(envId + "_master");
            drds.removeDataSource(envId + "_second");
        }
    }
}
