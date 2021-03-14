package com.cat.mapper;

import com.cat.entity.param.OperatingParameter;
import org.apache.ibatis.annotations.Mapper;

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
