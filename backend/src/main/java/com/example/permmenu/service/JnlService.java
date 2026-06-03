package com.example.permmenu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.permmenu.entity.TbspJnl;
import com.example.permmenu.entity.TbspData;
import java.util.List;

public interface JnlService {
    /**
     * 查询流水信息分页
     *
     * @param type     查询类型 (jnl 或 query)
     * @param trCode   交易编码
     * @param custNo   客户号 (必填，用于路由分表)
     * @param jnlStat  流水状态
     * @param channelNo 渠道号
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    IPage<TbspJnl> getJnlPage(String type, String trCode, String custNo, String jnlStat, String channelNo, int pageNo, int pageSize);

    /**
     * 根据客户号和流水号查询报文数据
     *
     * @param custNo   客户号
     * @param serialNo 流水号
     * @return 报文数据
     */
    TbspData getJnlData(String custNo, String serialNo);

    /**
     * 检查某个接口在指定的客户号列表中是否有成功调用记录
     *
     * @param trCode   接口编码
     * @param custNos  客户号列表
     * @param channelNo 渠道号
     * @return 是否有成功调用
     */
    boolean checkSuccess(String trCode, List<String> custNos, String channelNo);
}
