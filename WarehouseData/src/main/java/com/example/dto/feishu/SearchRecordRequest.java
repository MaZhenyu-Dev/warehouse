package com.example.dto.feishu;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.List;

/**
 * 记录查询请求
 */
@Data
public class SearchRecordRequest {
    
    /**
     * 要返回的字段名列表（为空则返回全部）
     */
    @JsonAlias("field_names")
    private List<String> fieldNames;
    
    /**
     * 是否返回自动字段（创建时间、更新时间等）
     */
    private Boolean automaticFields = false;
}