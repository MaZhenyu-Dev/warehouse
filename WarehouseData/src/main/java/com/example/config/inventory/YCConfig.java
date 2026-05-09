package com.example.config.inventory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yc") // 对应yml中的前缀
@Data // Lombok 自动生成 Getter/Setter
public class YCConfig {

    private String baseUrl;

    private String skuPath;

    private String inventoryPath;

    private String inboundOrderUrl;

    private String inboundBoxDetailUrl;

    // 新增
    private String outboundOrderPageListPath;

    private String appKey;

    private String appSecret;


    // 获取完整的库存查询URL
    public String getSKUUrl() {
        return baseUrl + skuPath;
    }

    // 获取完整的库存查询URL
    public String getInventoryUrl() {
        return baseUrl + inventoryPath;
    }

    // 拼接完整URL的辅助方法
    public String getFullInboundUrl() {
        return baseUrl + inboundOrderUrl;
    }

    // 新增：拼接装箱明细完整URL
    public String getFullInboundBoxDetailUrl() {
        return baseUrl + inboundBoxDetailUrl;
    }

    // 新增：出库单分页列表URL
    public String getOutboundOrderPageListUrl() {
        return baseUrl + outboundOrderPageListPath;
    }
}
