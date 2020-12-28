package com.cat.service;

import com.cat.dao.ParameterDao;
import com.cat.entity.param.OperatingParameter;
import com.cat.utils.ParamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        return Objects.requireNonNullElseGet(this.parameterDao.getLatestOperatingParameter(), ParamUtils::getDefaultParameter);
    }

    /**
     * 新增运行参数。
     *
     * @param orderDate      工单日期
     * @param fixedWidth     固定宽度
     * @param wasteThreshold 废料阈值
     * @param sortPattern    排序方式
     * @param orderModule    工单模块
     */
    public void insertOperatingParameter(LocalDate orderDate, BigDecimal fixedWidth, BigDecimal wasteThreshold, String sortPattern, String orderModule) {
        this.parameterDao.insertOperatingParameter(orderDate, fixedWidth, wasteThreshold, sortPattern, orderModule);
    }

    /**
     * 新增运行参数。
     *
     * @param parameter 运行参数对象
     */
    public void insertOperatingParameter(OperatingParameter parameter) {
        this.parameterDao.insertOperatingParameter(parameter.getOrderDate(), parameter.getFixedWidth(), parameter.getWasteThreshold(), parameter.getSortPattern(), parameter.getOrderModule());
    }
}
