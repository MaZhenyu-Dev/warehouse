package com.example.config.jack;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jackyun")
public class JackYunProperties {

    private String apiUrl;
    private Long appKey;
    private String appSecret;
    private String version;
    private String contentType;
}