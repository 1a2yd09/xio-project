package com.cat.service;

import com.cat.dao.TrimmingValueDao;
import com.cat.entity.TrimmingValue;
import com.cat.util.ParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TrimmingValueService {
    @Autowired
    TrimmingValueDao trimmingValueDao;

    public TrimmingValue getLatestTrimmingValue() {
        return Objects.requireNonNullElseGet(this.trimmingValueDao.getTrimmingValue(), ParamUtil::getDefaultValue);
    }
}
