package com.example.permmenu.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permmenu.dto.ProdFeatureVO;
import com.example.permmenu.entity.PermProdFeature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品功能 Mapper（强制使用 master 主库）
 */
@Mapper
@DS("master")
public interface PermProdFeatureMapper extends BaseMapper<PermProdFeature> {

    /**
     * 查询产品功能列表（关联产品名称，支持模糊筛选）
     *
     * @param tenantId 租户号
     * @param keyword  模糊搜索关键词（匹配产品名称 PROD_NAME 或功能名称 FEATURE_NAME）
     * @return 产品功能列表
     */
    List<ProdFeatureVO> selectProdFeatures(@Param("tenantId") String tenantId,
                                           @Param("keyword") String keyword);
}
