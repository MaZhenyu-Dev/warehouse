package com.example.controller;

import com.example.common.Result;
import com.example.service.FeishuSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class FeishuSkuController {

    @Autowired
    private FeishuSkuService feishuSkuService;

    /**
     * 统一入口，通过路径参数 {tableKey} 区分 lc 和 yc
     * 匹配: /warehouse-lc/records/search 和 /warehouse-yc/records/search
     */
    @GetMapping("/{tableKey}/records/searchSKU")
    public Result<Map<String, Object>> searchSku(@PathVariable("tableKey") String tableKey) {
        
        // 简单校验一下 key 是否合法 (可选，Service层其实也有校验)
        if (!"warehouse-lc".equals(tableKey) && !"warehouse-yc".equals(tableKey)) {
             throw new IllegalArgumentException("不支持的仓库类型: " + tableKey);
        }

        // 获取处理后的数据
        Map<String, Object> data = feishuSkuService.getSkuData(tableKey);

        // 返回统一格式
        return Result.success(data);
    }
}