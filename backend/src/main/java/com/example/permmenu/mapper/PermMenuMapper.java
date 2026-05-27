package com.example.permmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permmenu.entity.PermMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单权限 Mapper 接口
 */
public interface PermMenuMapper extends BaseMapper<PermMenu> {

    /**
     * 查询指定渠道的所有菜单
     *
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @return 菜单列表
     */
    List<PermMenu> selectByScope(@Param("menuScope") String menuScope,
                                 @Param("tenantId") String tenantId);

    /**
     * 根据复合主键查询菜单
     *
     * @param menuCode  菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @return 菜单实体
     */
    PermMenu selectByKey(@Param("menuCode") String menuCode,
                         @Param("menuScope") String menuScope,
                         @Param("tenantId") String tenantId);

    /**
     * 批量更新子菜单的上级编码
     *
     * @param oldCode   旧的上级菜单编码
     * @param newCode   新的上级菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @return 受影响行数
     */
    int updateUppMenuCode(@Param("oldCode") String oldCode,
                          @Param("newCode") String newCode,
                          @Param("menuScope") String menuScope,
                          @Param("tenantId") String tenantId);

    /**
     * 根据复合主键删除菜单
     *
     * @param menuCode  菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @return 受影响行数
     */
    int deleteByKey(@Param("menuCode") String menuCode,
                    @Param("menuScope") String menuScope,
                    @Param("tenantId") String tenantId);

    /**
     * 查询直接子菜单
     *
     * @param uppMenuCode 上级菜单编码
     * @param menuScope   菜单渠道
     * @param tenantId    租户号
     * @return 子菜单列表
     */
    List<PermMenu> selectChildren(@Param("uppMenuCode") String uppMenuCode,
                                  @Param("menuScope") String menuScope,
                                  @Param("tenantId") String tenantId);

    /**
     * 递归查询所有后代菜单（用于级联删除）
     *
     * @param uppMenuCode 上级菜单编码
     * @param menuScope   菜单渠道
     * @param tenantId    租户号
     * @return 所有后代菜单列表
     */
    List<PermMenu> selectAllDescendants(@Param("uppMenuCode") String uppMenuCode,
                                        @Param("menuScope") String menuScope,
                                        @Param("tenantId") String tenantId);

    /**
     * 插入菜单记录（自定义，处理复合主键）
     *
     * @param menu 菜单实体
     * @return 受影响行数
     */
    int insertMenu(@Param("menu") PermMenu menu);

    /**
     * 根据复合主键更新菜单（不更新主键字段）
     *
     * @param menu 菜单实体
     * @return 受影响行数
     */
    int updateByKey(@Param("menu") PermMenu menu);
}
