package com.example.permmenu.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permmenu.dto.MenuFeatureMountVO;
import com.example.permmenu.entity.PermFeatureMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单功能加挂关系 Mapper（强制使用 master 主库）
 */
@Mapper
@DS("master")
public interface PermFeatureMenuMapper extends BaseMapper<PermFeatureMenu> {

    /**
     * 查询指定渠道下所有被加挂的菜单编码列表
     *
     * @param menuScope 菜单渠道
     * @param tenantId  租户号
     * @return 已加挂的 MENU_CODE 列表
     */
    List<String> selectMountedMenuCodes(@Param("menuScope") String menuScope,
                                        @Param("tenantId") String tenantId);

    /**
     * 查询指定菜单被加挂的产品及功能详情列表
     *
     * @param menuScope 菜单渠道
     * @param menuCode  菜单编码
     * @param tenantId  租户号
     * @return 加挂关系详情列表
     */
    List<MenuFeatureMountVO> selectFeatureMountsByMenu(@Param("menuScope") String menuScope,
                                                       @Param("menuCode") String menuCode,
                                                       @Param("tenantId") String tenantId);

    /**
     * 检查是否已存在某菜单与功能加挂记录
     */
    int checkExistFeatureMenu(@Param("menuScope") String menuScope,
                              @Param("menuCode") String menuCode,
                              @Param("prodCode") String prodCode,
                              @Param("featureId") String featureId,
                              @Param("tenantId") String tenantId);

    /**
     * 插入加挂记录
     */
    int insertFeatureMenu(PermFeatureMenu featureMenu);

    /**
     * 删除指定的加挂记录
     */
    int deleteFeatureMenu(@Param("menuScope") String menuScope,
                          @Param("menuCode") String menuCode,
                          @Param("prodCode") String prodCode,
                          @Param("featureId") String featureId,
                          @Param("tenantId") String tenantId);
}
