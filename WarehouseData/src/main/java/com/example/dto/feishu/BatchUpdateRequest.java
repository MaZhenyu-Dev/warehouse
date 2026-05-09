package com.example.dto.feishu;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 通用批量更新请求
 */
@Data
public class BatchUpdateRequest {
    
    private List<UpdateItem> records;

    @Data
    public static class UpdateItem {
        /**
         * 记录ID（必填）
         */
        @JsonAlias("record_id")
        private String recordId;
        
        /**
         * 要更新的字段
         */
        private Map<String, Object> fields;
    }
}