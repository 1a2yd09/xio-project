package com.cat.service;

import com.cat.dao.ParameterDao;
import com.cat.entity.OperatingParameter;
import com.cat.util.ParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ParameterService {
    @Autowired
    ParameterDao parameterDao;

    public OperatingParameter getLatestOperatingParameter() {
        return Objects.requireNonNullElseGet(this.parameterDao.getOperatingParameter(), ParamUtil::getDefaultParameter);
    }
}
