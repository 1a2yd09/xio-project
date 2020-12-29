package com.cat;

import com.cat.enums.OrderModule;
import com.cat.service.MainService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author CAT
 */
@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource({"classpath:jdbc.properties", "classpath:smtp.properties"})
public class AppConfig {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        MainService mainService = context.getBean(MainService.class);
        try {
            mainService.start(OrderModule.BOTTOM_PLATFORM);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Bean
    DataSource createDataSource(@Value("${jdbc.url}") String jdbcUrl) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    JavaMailSender createJavaMailSender(
            @Value("${smtp.host}") String host,
            @Value("${smtp.port}") int port,
            @Value("${smtp.auth}") String auth,
            @Value("${smtp.username}") String username,
            @Value("${smtp.password}") String password,
            @Value("${smtp.debug:false}") String debug) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", debug);

        return mailSender;
    }
}
