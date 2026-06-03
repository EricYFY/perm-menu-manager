package com.example.permmenu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.TbspJnl;
import com.example.permmenu.entity.TbspData;
import com.example.permmenu.service.JnlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jnl")
@RequiredArgsConstructor
public class JnlController {

    private final JnlService jnlService;

    /**
     * 分页查询交易流水
     *
     * @param type     查询类型 (jnl 或 query)
     * @param trCode   交易编码
     * @param custNo   客户号
     * @param jnlStat  流水状态
     * @param channelNo 渠道号
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    @GetMapping("/page")
    public ResultVO<IPage<TbspJnl>> getJnlPage(
            @RequestParam(defaultValue = "jnl") String type,
            @RequestParam String trCode,
            @RequestParam String custNo,
            @RequestParam(required = false) String jnlStat,
            @RequestParam(required = false) String channelNo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            IPage<TbspJnl> page = jnlService.getJnlPage(type, trCode, custNo, jnlStat, channelNo, pageNo, pageSize);
            return ResultVO.success(page);
        } catch (IllegalArgumentException e) {
            return ResultVO.error(e.getMessage());
        } catch (Exception e) {
            return ResultVO.error("查询流水异常: " + e.getMessage());
        }
    }

    /**
     * 查询流水关联的报文数据
     *
     * @param custNo   客户号
     * @param serialNo 流水号
     * @return 报文数据
     */
    @GetMapping("/data")
    public ResultVO<TbspData> getJnlData(
            @RequestParam String custNo,
            @RequestParam String serialNo) {
        try {
            TbspData data = jnlService.getJnlData(custNo, serialNo);
            return ResultVO.success(data);
        } catch (IllegalArgumentException e) {
            return ResultVO.error(e.getMessage());
        } catch (Exception e) {
            return ResultVO.error("查询报文异常: " + e.getMessage());
        }
    }

    /**
     * 检查接口是否有成功调用记录
     *
     * @param trCode   接口编码
     * @param custNos  客户号列表
     * @param channelNo 渠道号
     * @return 是否有成功调用
     */
    @GetMapping("/check-success")
    public ResultVO<Boolean> checkSuccess(
            @RequestParam String trCode,
            @RequestParam List<String> custNos,
            @RequestParam(required = false) String channelNo) {
        try {
            boolean isSuccess = jnlService.checkSuccess(trCode, custNos, channelNo);
            return ResultVO.success(isSuccess);
        } catch (Exception e) {
            return ResultVO.error("校验成功状态异常: " + e.getMessage());
        }
    }
}
