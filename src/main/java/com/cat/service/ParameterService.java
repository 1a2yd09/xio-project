package com.cat.service;

import com.cat.mapper.ParameterMapper;
import com.cat.pojo.OperatingParameter;
import com.cat.utils.ParamUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author CAT
 * 对于数据表tb_operating_parameter的查询，不同的工单设置的参数可能不一样，例如waste_threshold（废料阈值）、order_module（工单模块）
 */
@Service
public class ParameterService {
    private final ParameterMapper parameterMapper;

    public ParameterService(ParameterMapper parameterMapper) {
        this.parameterMapper = parameterMapper;
    }

    /**
     * 查询最新的运行参数。
     *
     * @return 运行参数
     */
    public OperatingParameter getLatestOperatingParameter() {
        return Objects.requireNonNullElseGet(this.parameterMapper.getLatestOperatingParameter(), ParamUtil::getDefaultParameter);
    }

    /**
     * 新增运行参数。
     *
     * @param parameter 运行参数对象
     */
    public void insertOperatingParameter(OperatingParameter parameter) {
        this.parameterMapper.insertOperatingParameter(parameter);
    }
}
