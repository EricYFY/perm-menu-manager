package com.example.permmenu.controller;

import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.ComDict;
import com.example.permmenu.service.ComDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 */
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class ComDictController {

    private final ComDictService comDictService;

    /**
     * 获取所有 DICT_ID 列表
     *
     * @param tenantId 租户号，默认 047
     * @return DICT_ID 列表
     */
    @GetMapping("/ids")
    public ResultVO<List<ComDict>> listDictIds(
            @RequestParam(defaultValue = "047") String tenantId) {
        try {
            List<ComDict> ids = comDictService.listDictIds(tenantId);
            return ResultVO.success(ids);
        } catch (Exception e) {
            return ResultVO.error("查询字典ID列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取某个 DICT_ID 下的所有字典条目
     *
     * @param dictId   字典ID
     * @param tenantId 租户号，默认 047
     * @return 字典条目列表
     */
    @GetMapping("/{dictId}")
    public ResultVO<List<ComDict>> listByDictId(
            @PathVariable String dictId,
            @RequestParam(defaultValue = "047") String tenantId) {
        try {
            List<ComDict> list = comDictService.listByDictId(dictId, tenantId);
            return ResultVO.success(list);
        } catch (Exception e) {
            return ResultVO.error("查询字典条目失败：" + e.getMessage());
        }
    }

    /**
     * 新增字典条目
     *
     * @param dict 字典实体
     * @return 操作结果
     */
    @PostMapping
    public ResultVO<Void> addDictEntry(@RequestBody ComDict dict) {
        try {
            comDictService.addDictEntry(dict);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("新增字典条目失败：" + e.getMessage());
        }
    }

    /**
     * 更新字典条目
     *
     * @param dict 字典实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public ResultVO<Void> updateDictEntry(@RequestBody ComDict dict) {
        try {
            comDictService.updateDictEntry(dict);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("更新字典条目失败：" + e.getMessage());
        }
    }

    /**
     * 删除单条字典记录
     *
     * @param dictId   字典ID
     * @param dictKey  字典键值
     * @param tenantId 租户号，默认 047
     * @return 操作结果
     */
    @PostMapping("/delete/{dictId}/{dictKey}")
    public ResultVO<Void> deleteDictEntry(
            @PathVariable String dictId,
            @PathVariable String dictKey,
            @RequestParam(defaultValue = "047") String tenantId) {
        try {
            comDictService.deleteDictEntry(dictId, dictKey, tenantId);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("删除字典条目失败：" + e.getMessage());
        }
    }

    /**
     * 删除整个 DICT_ID 组（及其下所有条目）
     *
     * @param dictId   字典ID
     * @param tenantId 租户号，默认 047
     * @return 操作结果
     */
    @PostMapping("/delete/{dictId}")
    public ResultVO<Void> deleteDictGroup(
            @PathVariable String dictId,
            @RequestParam(defaultValue = "047") String tenantId) {
        try {
            comDictService.deleteDictGroup(dictId, tenantId);
            return ResultVO.success();
        } catch (Exception e) {
            return ResultVO.error("删除字典组失败：" + e.getMessage());
        }
    }
}
