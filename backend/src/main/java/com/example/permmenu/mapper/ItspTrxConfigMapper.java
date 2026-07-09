package com.example.permmenu.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permmenu.entity.ItspTrxConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
@DS("second")
public interface ItspTrxConfigMapper extends BaseMapper<ItspTrxConfig> {

    void updateByKeys(@Param("config") ItspTrxConfig config);

    void deleteByKeys(@Param("trCode") String trCode, @Param("language") String language);
}
