package com.example.dto.feishu;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 通用批量创建请求
 */
@Data
public class BatchCreateRequest {
    
    /**
     * 记录列表
     * 每条记录是一个 Map，Key 是字段名（中文），Value 是字段值
     */
    private List<Map<String, Object>> records;
}