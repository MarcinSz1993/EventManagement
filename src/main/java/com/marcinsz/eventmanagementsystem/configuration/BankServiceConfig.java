package com.marcinsz.eventmanagementsystem.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "bankservice")
public class BankServiceConfig {
    private String url;
    private String userLogin;
    private String transaction;
}
