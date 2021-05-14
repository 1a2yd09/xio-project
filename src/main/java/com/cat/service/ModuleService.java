package com.cat.service;

import com.cat.pojo.OperatingParameter;

/**
 * @author CAT
 */
public interface ModuleService {
    /**
     * 启动模块套料流程。
     *
     * @param param 运行参数
     */
    void process(OperatingParameter param);
}
