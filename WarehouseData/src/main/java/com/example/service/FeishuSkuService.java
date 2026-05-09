package com.example.service;

import java.util.Map;

public interface FeishuSkuService {
    /**
     * 根据表标识获取清洗后的SKU数据
     * @param tableKey 表标识 (warehouse-lc / warehouse-yc)
     * @return 封装好的Map数据，用于直接返回给前端
     */
    Map<String, Object> getSkuData(String tableKey);
}