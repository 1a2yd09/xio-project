package com.cat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailConfig {
    private Integer id;
    private String host;
    private Integer port;
    private Boolean auth;
    private String username;
    private String password;
    private String sendFrom;
    private String sendTo;
    private LocalDateTime createdAt;
}
