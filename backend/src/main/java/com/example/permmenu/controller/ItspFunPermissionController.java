package com.example.permmenu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.permmenu.dto.ResultVO;
import com.example.permmenu.entity.ItspFunPermission;
import com.example.permmenu.service.ItspFunPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/fun-permission")
public class ItspFunPermissionController {

    @Autowired
    private ItspFunPermissionService itspFunPermissionService;

    @Autowired
    private com.example.permmenu.mapper.ItspFunPermissionMapper itspFunPermissionMapper;

    @GetMapping("/options/{field}")
    public ResultVO<java.util.List<java.util.Map<String, String>>> getOptions(@PathVariable String field) {
        String col = "";
        String colDesc = "";
        if ("transModule".equals(field)) { col = "trans_module"; colDesc = "trans_module_desc"; }
        else if ("busiType".equals(field)) { col = "busi_type"; colDesc = "busi_type_desc"; }
        else if ("userRole".equals(field)) { col = "user_role"; colDesc = "user_role_desc"; }
        else if ("funButton".equals(field)) { col = "fun_button"; colDesc = "fun_button_desc"; }
        else { return ResultVO.error("Invalid field"); }
        
        java.util.List<java.util.Map<String, String>> options = itspFunPermissionMapper.getDistinctOptions(col, colDesc);
        return ResultVO.success(options);
    }

    @GetMapping("/group-page")
    public ResultVO<?> groupPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String transModule,
            @RequestParam(required = false) String busiType,
            @RequestParam(required = false) String userRole) {
        ItspFunPermission param = new ItspFunPermission();
        param.setTransModule(transModule);
        param.setBusiType(busiType);
        param.setUserRole(userRole);
        
        com.baomidou.mybatisplus.core.metadata.IPage<com.example.permmenu.dto.FunPermissionGroupVO> page = 
            itspFunPermissionMapper.selectGroupPage(new Page<>(current, size), param);
        return ResultVO.success(page);
    }

    @GetMapping("/page")
    public ResultVO<?> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String transModule,
            @RequestParam(required = false) String busiType,
            @RequestParam(required = false) String userRole) {
        LambdaQueryWrapper<ItspFunPermission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(transModule)) {
            wrapper.eq(ItspFunPermission::getTransModule, transModule);
        }
        if (StringUtils.hasText(busiType)) {
            wrapper.eq(ItspFunPermission::getBusiType, busiType);
        }
        if (StringUtils.hasText(userRole)) {
            wrapper.eq(ItspFunPermission::getUserRole, userRole);
        }
        wrapper.orderByAsc(ItspFunPermission::getSeq);
        
        Page<ItspFunPermission> page = itspFunPermissionService.page(new Page<>(current, size), wrapper);
        return ResultVO.success(page);
    }

    @PostMapping
    public ResultVO<Boolean> save(@RequestBody ItspFunPermission itspFunPermission) {
        if (!StringUtils.hasText(itspFunPermission.getPermissionId())) {
            itspFunPermission.setPermissionId(UUID.randomUUID().toString().replace("-", ""));
        }
        return ResultVO.success(itspFunPermissionService.save(itspFunPermission));
    }

    @PostMapping("/batch")
    public ResultVO<Boolean> saveBatch(@RequestBody java.util.List<ItspFunPermission> list) {
        for (ItspFunPermission itspFunPermission : list) {
            if (!StringUtils.hasText(itspFunPermission.getPermissionId())) {
                itspFunPermission.setPermissionId(UUID.randomUUID().toString().replace("-", ""));
            }
        }
        return ResultVO.success(itspFunPermissionService.saveBatch(list));
    }

    @PutMapping
    public ResultVO<Boolean> update(@RequestBody ItspFunPermission itspFunPermission) {
        return ResultVO.success(itspFunPermissionService.updateById(itspFunPermission));
    }

    @DeleteMapping("/{id}")
    public ResultVO<Boolean> delete(@PathVariable String id) {
        return ResultVO.success(itspFunPermissionService.removeById(id));
    }
}
