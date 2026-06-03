package com.example.permmenu.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.permmenu.entity.TbspJnl;
import com.example.permmenu.entity.TbspData;
import com.example.permmenu.mapper.JnlMapper;
import com.example.permmenu.service.JnlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JnlServiceImpl implements JnlService {

    private final JnlMapper jnlMapper;

    @Override
    public IPage<TbspJnl> getJnlPage(String type, String trCode, String custNo, String jnlStat, String channelNo, int pageNo, int pageSize) {
        if (custNo == null || custNo.length() < 2) {
            throw new IllegalArgumentException("客户号无效或未提供，无法确定查询分表");
        }

        // 计算分表后缀
        String suffix = custNo.substring(custNo.length() - 2);

        // 确定表名
        String tableName;
        if ("query".equalsIgnoreCase(type)) {
            tableName = "tbsp_jnl_query_" + suffix;
        } else {
            tableName = "tbsp_jnl_" + suffix;
        }

        Page<TbspJnl> page = new Page<>(pageNo, pageSize);
        return jnlMapper.selectJnlPage(page, tableName, trCode, custNo, jnlStat, channelNo);
    }

    @Override
    public TbspData getJnlData(String custNo, String serialNo) {
        if (custNo == null || custNo.length() < 2) {
            throw new IllegalArgumentException("客户号无效或未提供，无法确定查询分表");
        }
        String suffix = custNo.substring(custNo.length() - 2);
        String tableName = "tbsp_data_" + suffix;
        return jnlMapper.selectTbspData(tableName, serialNo);
    }

    @Override
    public boolean checkSuccess(String trCode, List<String> custNos, String channelNo) {
        if (custNos == null || custNos.isEmpty() || trCode == null || trCode.trim().isEmpty()) {
            return false;
        }

        for (String custNo : custNos) {
            if (custNo == null || custNo.length() < 2) {
                continue; // Skip invalid custNo
            }
            String suffix = custNo.substring(custNo.length() - 2);
            
            // Check tbsp_jnl_XX
            String jnlTable = "tbsp_jnl_" + suffix;
            try {
                Integer count = jnlMapper.countSuccess(jnlTable, trCode, custNo, channelNo);
                if (count != null && count > 0) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("检查表 {} 失败: {}", jnlTable, e.getMessage());
            }

            // Check tbsp_jnl_query_XX
            String queryTable = "tbsp_jnl_query_" + suffix;
            try {
                Integer count = jnlMapper.countSuccess(queryTable, trCode, custNo, channelNo);
                if (count != null && count > 0) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("检查表 {} 失败: {}", queryTable, e.getMessage());
            }
        }
        return false;
    }
}
