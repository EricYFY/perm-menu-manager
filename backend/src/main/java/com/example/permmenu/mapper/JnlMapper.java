package com.example.permmenu.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.permmenu.entity.TbspJnl;
import com.example.permmenu.entity.TbspData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JnlMapper {

    /**
     * 分页查询流水信息
     *
     * @param page      分页对象
     * @param tableName 动态表名
     * @param trCode    交易编码
     * @param custNo    客户号
     * @param jnlStat   流水状态
     * @param channelNo 渠道号
     * @return 分页数据
     */
    IPage<TbspJnl> selectJnlPage(IPage<TbspJnl> page,
                                 @Param("tableName") String tableName,
                                 @Param("trCode") String trCode,
                                 @Param("custNo") String custNo,
                                 @Param("jnlStat") String jnlStat,
                                 @Param("channelNo") String channelNo);

    /**
     * 查询流水报文数据
     *
     * @param tableName 动态表名
     * @param serialNo  流水号
     * @return 报文数据
     */
    TbspData selectTbspData(@Param("tableName") String tableName,
                            @Param("serialNo") String serialNo);

    /**
     * 检查是否在指定分表中有该客户的成功流水
     *
     * @param tableName 分表名
     * @param trCode    接口编码
     * @param custNo    客户号
     * @param channelNo 渠道号
     * @return 匹配记录数
     */
    Integer countSuccess(@Param("tableName") String tableName,
                         @Param("trCode") String trCode,
                         @Param("custNo") String custNo,
                         @Param("channelNo") String channelNo);
}
