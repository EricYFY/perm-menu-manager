package com.example.permmenu.service.impl;

import com.example.permmenu.entity.ComDict;
import com.example.permmenu.mapper.ComDictMapper;
import com.example.permmenu.service.ComDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务实现类
 */
@Service
@RequiredArgsConstructor
public class ComDictServiceImpl implements ComDictService {

    private final ComDictMapper comDictMapper;

    @Override
    public List<ComDict> listDictIds(String tenantId) {
        return comDictMapper.selectDistinctDictIds(tenantId);
    }

    @Override
    public List<ComDict> listByDictId(String dictId, String tenantId) {
        return comDictMapper.selectByDictId(dictId, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDictEntry(ComDict dict) {
        // 检查是否已存在相同主键
        ComDict existing = comDictMapper.selectByKey(dict.getDictId(), dict.getDictKey(), dict.getTenantId());
        if (existing != null) {
            throw new RuntimeException("字典条目已存在：DICT_ID=" + dict.getDictId() + ", DICT_KEY=" + dict.getDictKey());
        }
        // 设置默认值
        if (dict.getStat() == null || dict.getStat().isEmpty()) {
            dict.setStat("1");
        }
        comDictMapper.insertDict(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictEntry(ComDict dict) {
        ComDict existing = comDictMapper.selectByKey(dict.getDictId(), dict.getDictKey(), dict.getTenantId());
        if (existing == null) {
            throw new RuntimeException("字典条目不存在：DICT_ID=" + dict.getDictId() + ", DICT_KEY=" + dict.getDictKey());
        }
        comDictMapper.updateByKey(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictEntry(String dictId, String dictKey, String tenantId) {
        comDictMapper.deleteByKey(dictId, dictKey, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictGroup(String dictId, String tenantId) {
        comDictMapper.deleteByDictId(dictId, tenantId);
    }
}
