package com.cat.utils;

import com.cat.service.ModuleService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author CAT
 */
@Component
public class ModuleServiceFactory implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public ModuleService getModuleService(String beanName) {
        return this.context.getBean(beanName, ModuleService.class);
    }
}
