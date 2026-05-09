package com.example.config.feishu;

/**
 * 飞书表配置统一接口
 */
public interface FeiShuTableConfig {
    
    String getAppId();
    
    String getAppSecret();
    
    String getBitableAppToken();
    
    String getTableId();
    
    String getViewId();
}