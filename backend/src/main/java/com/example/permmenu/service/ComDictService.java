package com.example.permmenu.service;

import com.example.permmenu.entity.ComDict;

import java.util.List;

/**
 * 字典服务接口
 */
public interface ComDictService {

    /**
     * 查询所有 DICT_ID 列表
     *
     * @param tenantId 租户号
     * @return DICT_ID 列表
     */
    List<ComDict> listDictIds(String tenantId);

    /**
     * 查询某个 DICT_ID 下的所有字典条目
     *
     * @param dictId   字典ID
     * @param tenantId 租户号
     * @return 字典条目列表
     */
    List<ComDict> listByDictId(String dictId, String tenantId);

    /**
     * 新增字典条目
     *
     * @param dict 字典实体
     */
    void addDictEntry(ComDict dict);

    /**
     * 更新字典条目
     *
     * @param dict 字典实体
     */
    void updateDictEntry(ComDict dict);

    /**
     * 删除单条字典记录
     *
     * @param dictId   字典ID
     * @param dictKey  字典键值
     * @param tenantId 租户号
     */
    void deleteDictEntry(String dictId, String dictKey, String tenantId);

    /**
     * 删除整个 DICT_ID（及其下所有条目）
     *
     * @param dictId   字典ID
     * @param tenantId 租户号
     */
    void deleteDictGroup(String dictId, String tenantId);
}
