package com.cat.mapper;

import com.cat.pojo.OperatingParameter;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author CAT
 */
@Mapper
public interface ParameterMapper {
    /**
     * 查询最新的运行参数，数据表为空时将返回 null。
     *
     * @return 运行参数
     */
    OperatingParameter getLatestOperatingParameter();

    /**
     * 新增运行参数。
     *
     * @param parameter 运行参数对象
     */
    void insertOperatingParameter(OperatingParameter parameter);
}
