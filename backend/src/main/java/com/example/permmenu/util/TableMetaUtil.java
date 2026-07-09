package com.example.permmenu.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库表元数据工具类
 * 用于动态检查表中是否存在特定字段
 */
@Component
public class TableMetaUtil {

    @Autowired
    private DataSource dataSource;

    private static DataSource staticDataSource;

    // Cache: TableName -> (ColumnName -> Boolean)
    private static final Map<String, Map<String, Boolean>> columnCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        staticDataSource = this.dataSource;
    }

    /**
     * 判断表中是否存在指定的列
     *
     * @param tableName  表名
     * @param columnName 列名
     * @return true 如果列存在，否则 false
     */
    public static boolean hasColumn(String tableName, String columnName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = "perm_menu";
        }
        if (columnName == null || columnName.trim().isEmpty()) {
            return false;
        }

        String tName = tableName;
        String cName = columnName;
        
        // 关键点：多数据源环境下，必须隔离不同数据源的缓存，否则一个环境的探测结果会污染另一个环境
        String dsKey = com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.peek();
        if (dsKey == null) {
            dsKey = "default";
        }
        String cacheKey = dsKey + ":" + tName;

        Map<String, Boolean> tableCache = columnCache.computeIfAbsent(cacheKey, k -> new ConcurrentHashMap<>());

        if (tableCache.containsKey(cName)) {
            return tableCache.get(cName);
        }

        boolean exists = false;
        try (Connection conn = staticDataSource.getConnection()) {
            String catalog = conn.getCatalog();
            
            // 尝试直接获取
            ResultSet rs = conn.getMetaData().getColumns(catalog, null, tName, cName);
            if (rs.next()) {
                exists = true;
            } else {
                // 尝试转大写获取 (Oracle等默认大写)
                rs = conn.getMetaData().getColumns(catalog, null, tName.toUpperCase(), cName.toUpperCase());
                if (rs.next()) {
                    exists = true;
                } else {
                    // 尝试转小写获取 (MySQL/PG等可能小写)
                    rs = conn.getMetaData().getColumns(catalog, null, tName.toLowerCase(), cName.toLowerCase());
                    if (rs.next()) {
                        exists = true;
                    }
                }
            }
        } catch (Exception e) {
            // 获取失败，默认视为不存在
        }

        tableCache.put(cName, exists);
        return exists;
    }
}
