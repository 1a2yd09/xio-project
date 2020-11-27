package com.cat.service;

import com.cat.dao.ParameterDao;
import com.cat.entity.param.OperatingParameter;
import com.cat.utils.ParamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author CAT
 */
@Component
public class ParameterService {
    @Autowired
    ParameterDao parameterDao;

    /**
     * 查询最新的运行参数。
     *
     * @return 运行参数
     */
    public OperatingParameter getLatestOperatingParameter() {
        // 返回值为空时，返回一个默认的运行参数:
        return Objects.requireNonNullElseGet(this.parameterDao.getLatestOperatingParameter(), ParamUtils::getDefaultParameter);
    }
}
