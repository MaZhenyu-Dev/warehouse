package com.example.config.inventory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jf") // 对应yml中的前缀
@Data // Lombok 自动生成 Getter/Setter
public class JFConfig {
    private String url;
    private String appToken;
    private String appKey;
}
