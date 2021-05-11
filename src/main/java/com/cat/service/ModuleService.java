package com.cat.service;

import com.cat.pojo.OperatingParameter;

/**
 * @author CAT
 */
public interface ModuleService {
    /**
     * 处理工单集合。
     *
     * @param param 运行参数
     */
    void processOrderCollection(OperatingParameter param);
}
