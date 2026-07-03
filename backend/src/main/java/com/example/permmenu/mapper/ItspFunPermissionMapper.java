package com.example.permmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.permmenu.entity.ItspFunPermission;
import com.example.permmenu.dto.FunPermissionGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ItspFunPermissionMapper extends BaseMapper<ItspFunPermission> {
    @Select("SELECT DISTINCT ${field} AS code, ${fieldDesc} AS `desc` FROM itsp_fun_permission WHERE ${field} IS NOT NULL AND ${field} != ''")
    List<Map<String, String>> getDistinctOptions(@Param("field") String field, @Param("fieldDesc") String fieldDesc);

    @Select("<script>" +
            "SELECT trans_module as transModule, trans_module_desc as transModuleDesc, " +
            "busi_type as busiType, busi_type_desc as busiTypeDesc, " +
            "user_role as userRole, user_role_desc as userRoleDesc, " +
            "COUNT(permission_id) as btnCount " +
            "FROM itsp_fun_permission " +
            "<where> " +
            "<if test='param.transModule != null and param.transModule != \"\"'> AND trans_module = #{param.transModule} </if> " +
            "<if test='param.busiType != null and param.busiType != \"\"'> AND busi_type = #{param.busiType} </if> " +
            "<if test='param.userRole != null and param.userRole != \"\"'> AND user_role = #{param.userRole} </if> " +
            "</where> " +
            "GROUP BY trans_module, trans_module_desc, busi_type, busi_type_desc, user_role, user_role_desc " +
            "</script>")
    IPage<FunPermissionGroupVO> selectGroupPage(IPage<?> page, @Param("param") ItspFunPermission param);
}
