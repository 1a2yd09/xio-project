package com.cat.mapper;

import com.cat.pojo.MailConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author CAT
 */
@Mapper
public interface MailMapper {
    /**
     * 获取最新的邮箱配置记录。
     *
     * @return 邮箱配置对象
     */
    MailConfig getLatestMailConfig();
}
