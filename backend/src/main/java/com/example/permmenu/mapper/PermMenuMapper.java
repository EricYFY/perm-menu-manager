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
     * @param menuScope     菜单渠道
     * @param tenantId      租户号
     * @param tableName     表名（支持动态切换正式表/临时表）
     * @param subsystemCode 子系统编码（可选过滤条件）
     * @return 菜单列表
     */
    List<PermMenu> selectByScope(@Param("menuScope") String menuScope,
                                 @Param("tenantId") String tenantId,
                                 @Param("tableName") String tableName,
                                 @Param("subsystemCode") String subsystemCode);

    /**
     * 根据复合主键查询菜单
     *
     * @param menuCode  菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @param tableName 表名
     * @return 菜单实体
     */
    PermMenu selectByKey(@Param("menuCode") String menuCode,
                         @Param("menuScope") String menuScope,
                         @Param("tenantId") String tenantId,
                         @Param("tableName") String tableName);

    /**
     * 批量更新子菜单的上级编码
     *
     * @param oldCode   旧的上级菜单编码
     * @param newCode   新的上级菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @param tableName 表名
     * @return 受影响行数
     */
    int updateUppMenuCode(@Param("oldCode") String oldCode,
                          @Param("newCode") String newCode,
                          @Param("menuScope") String menuScope,
                          @Param("tenantId") String tenantId,
                          @Param("tableName") String tableName);

    /**
     * 根据复合主键删除菜单
     *
     * @param menuCode  菜单编码
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @param tableName 表名
     * @return 受影响行数
     */
    int deleteByKey(@Param("menuCode") String menuCode,
                    @Param("menuScope") String menuScope,
                    @Param("tenantId") String tenantId,
                    @Param("tableName") String tableName);

    /**
     * 查询直接子菜单
     *
     * @param uppMenuCode 上级菜单编码
     * @param menuScope   菜单渠道
     * @param tenantId    租户号
     * @param tableName   表名
     * @return 子菜单列表
     */
    List<PermMenu> selectChildren(@Param("uppMenuCode") String uppMenuCode,
                                  @Param("menuScope") String menuScope,
                                  @Param("tenantId") String tenantId,
                                  @Param("tableName") String tableName);

    /**
     * 递归查询所有后代菜单（用于级联删除）
     *
     * @param uppMenuCode 上级菜单编码
     * @param menuScope   菜单渠道
     * @param tenantId    租户号
     * @param tableName   表名
     * @return 所有后代菜单列表
     */
    List<PermMenu> selectAllDescendants(@Param("uppMenuCode") String uppMenuCode,
                                        @Param("menuScope") String menuScope,
                                        @Param("tenantId") String tenantId,
                                        @Param("tableName") String tableName);

    /**
     * 插入菜单记录（自定义，处理复合主键）
     *
     * @param menu      菜单实体
     * @param tableName 表名
     * @return 受影响行数
     */
    int insertMenu(@Param("menu") PermMenu menu,
                   @Param("tableName") String tableName);

    /**
     * 根据复合主键更新菜单（不更新主键字段）
     *
     * @param menu      菜单实体
     * @param tableName 表名
     * @return 受影响行数
     */
    int updateByKey(@Param("menu") PermMenu menu,
                    @Param("tableName") String tableName);

    /**
     * 查询指定表的全部数据（用于一致性比对）
     *
     * @param tableName 表名
     * @return 菜单列表
     */
    List<PermMenu> selectAll(@Param("tableName") String tableName);

    /**
     * 按子系统查询指定表的所有数据（用于带过滤条件的一致性比对）
     *
     * @param tableName     表名
     * @param subsystemCode 子系统编码
     * @return 菜单列表
     */
    List<PermMenu> selectAllBySubsystem(@Param("tableName") String tableName,
                                        @Param("subsystemCode") String subsystemCode);
}
