package com.example.dto.feishu;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.List;

/**
 * 批量添加字段请求
 */
@Data
public class AddFieldRequest {

    @JsonAlias("field_names")
    private List<String> fieldNames;
    
    private List<Integer> types;
}