package com.example.config.feishu;

import com.lark.oapi.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 飞书配置注册中心
 * 统一管理所有表的配置和客户端
 */
@Component
public class FeiShuConfigRegistry {

    private final Map<String, FeiShuTableConfig> configMap = new HashMap<>();
    private final Map<String, Client> clientMap = new HashMap<>();

    @Autowired
    private FeiShuLCSKUConfig LCSKUConfig;

    @Autowired
    private FeiShuYCSKUConfig YCSKUConfig;

    @Autowired
    private FeiShuLCWarehouseConfig LCWarehouseConfig;

    @Autowired
    private FeiShuYCWarehouseConfig YCWarehouseConfig;

    // 如果新增表，在这里 @Autowired 新的配置类

    @PostConstruct
    public void init() {
        // 注册所有表
        register("warehouse-lc", LCSKUConfig);
        register("warehouse-yc", YCSKUConfig);
        register("warehouse-lc-sync", LCWarehouseConfig);
        register("warehouse-yc-sync", YCWarehouseConfig);
        // 新增表时加一行：register("order", orderConfig);
    }

    /**
     * 注册表配置
     */
    private void register(String tableKey, FeiShuTableConfig config) {
        configMap.put(tableKey, config);
        // 为每个配置创建独立的 Client
        Client client = Client.newBuilder(config.getAppId(), config.getAppSecret()).build();
        clientMap.put(tableKey, client);
    }

    /**
     * 获取配置
     */
    public FeiShuTableConfig getConfig(String tableKey) {
        FeiShuTableConfig config = configMap.get(tableKey);
        if (config == null) {
            throw new IllegalArgumentException("未知的表标识: " + tableKey + 
                    "，可用值: " + configMap.keySet());
        }
        return config;
    }

    /**
     * 获取客户端
     */
    public Client getClient(String tableKey) {
        Client client = clientMap.get(tableKey);
        if (client == null) {
            throw new IllegalArgumentException("未知的表标识: " + tableKey);
        }
        return client;
    }

    /**
     * 获取所有已注册的表名
     */
    public Set<String> getAllTableKeys() {
        return configMap.keySet();
    }

    /**
     * 检查表是否存在
     */
    public boolean hasTable(String tableKey) {
        return configMap.containsKey(tableKey);
    }
}
