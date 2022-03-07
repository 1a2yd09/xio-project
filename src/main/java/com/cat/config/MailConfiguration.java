package com.cat.config;

import com.cat.mapper.MailMapper;
import com.cat.pojo.MailConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * @author CAT
 */
//@Configuration
public class MailConfiguration {
    @Bean
    JavaMailSender createJavaMailSender(MailMapper mailMapper) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        MailConfig config = mailMapper.getLatestMailConfig();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", config.getAuth());
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", false);

        return mailSender;
    }
}
