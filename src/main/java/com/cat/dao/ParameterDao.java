package com.cat.dao;

import com.cat.entity.param.OperatingParameter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author CAT
 */
@Component
public class ParameterDao extends BaseDao {
    /**
     * 查询最新的运行参数，数据表为空时将返回 null
     *
     * @return 运行参数
     */
    public OperatingParameter getLatestOperatingParameter() {
        List<OperatingParameter> list = jdbcTemplate.query("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY id DESC", new BeanPropertyRowMapper<>(OperatingParameter.class));
        return list.isEmpty() ? null : list.get(0);
    }
}
