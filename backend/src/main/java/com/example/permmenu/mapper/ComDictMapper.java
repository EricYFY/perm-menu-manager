package com.example.permmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permmenu.entity.ComDict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典 Mapper 接口
 */
public interface ComDictMapper extends BaseMapper<ComDict> {

    /**
     * 查询所有不同的 DICT_ID 列表（用于列表页展示）
     *
     * @param tenantId 租户号
     * @return DICT_ID 列表
     */
    List<ComDict> selectDistinctDictIds(@Param("tenantId") String tenantId);

    /**
     * 根据 DICT_ID 查询其下所有字典条目
     *
     * @param dictId   字典ID
     * @param tenantId 租户号
     * @return 字典条目列表
     */
    List<ComDict> selectByDictId(@Param("dictId") String dictId,
                                  @Param("tenantId") String tenantId);

    /**
     * 根据复合主键查询单条字典记录
     *
     * @param dictId   字典ID
     * @param dictKey  字典键值
     * @param tenantId 租户号
     * @return 字典实体
     */
    ComDict selectByKey(@Param("dictId") String dictId,
                        @Param("dictKey") String dictKey,
                        @Param("tenantId") String tenantId);

    /**
     * 插入字典记录
     *
     * @param dict 字典实体
     * @return 受影响行数
     */
    int insertDict(@Param("dict") ComDict dict);

    /**
     * 根据复合主键更新字典记录
     *
     * @param dict 字典实体
     * @return 受影响行数
     */
    int updateByKey(@Param("dict") ComDict dict);

    /**
     * 根据复合主键删除字典记录
     *
     * @param dictId   字典ID
     * @param dictKey  字典键值
     * @param tenantId 租户号
     * @return 受影响行数
     */
    int deleteByKey(@Param("dictId") String dictId,
                    @Param("dictKey") String dictKey,
                    @Param("tenantId") String tenantId);

    /**
     * 删除整个 DICT_ID 下的所有记录
     *
     * @param dictId   字典ID
     * @param tenantId 租户号
     * @return 受影响行数
     */
    int deleteByDictId(@Param("dictId") String dictId,
                        @Param("tenantId") String tenantId);
}
