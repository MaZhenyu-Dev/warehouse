package com.example.config.feishu;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "feishu.lepan-warehouse-yc-sync")
public class FeiShuYCWarehouseConfig implements FeiShuTableConfig {
    private String appId;
    private String appSecret;
    private String bitableAppToken;
    private String tableId;
    private String viewId;
}
