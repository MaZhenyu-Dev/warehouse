package com.example.service.impl;

import com.example.dto.feishu.SearchRecordRequest;
import com.example.service.FeiOperationsService;
import com.example.service.FeishuSkuService;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeishuSkuServiceImpl implements FeishuSkuService {

    // 假设你之前的 searchAllRecords 方法所在的类叫 FeishuBaseService
    // 如果是在当前类，直接调用即可；如果是其他Service，请注入
    @Autowired
    private FeiOperationsService baseService;

    @Override
    public Map<String, Object> getSkuData(String tableKey) {
        // 1. 准备查询请求：为了性能，建议只申请 "SKU" 字段
        SearchRecordRequest request = new SearchRecordRequest();
        request.setFieldNames(Collections.singletonList("SKU")); // 只查SKU列，减少流量

        // 2. 调用已有的通用查询方法
        List<AppTableRecord> records = baseService.searchAllRecords(tableKey, request);

        // 3. 提取所有 SKU 的 text 值
        List<String> skuList = records.stream()
                .map(this::extractSkuText)     // 解析复杂结构
                .filter(Objects::nonNull)      // 过滤掉空值
                .filter(s -> !s.isEmpty())     // 过滤空字符串
                .collect(Collectors.toList());

        // 4. 根据 tableKey 组装不同的返回格式
        Map<String, Object> resultMap = new HashMap<>();

        if ("warehouse-lc".equals(tableKey)) {
            // 场景 A: 返回数组，key 为 product_sku_arr
            resultMap.put("product_sku_arr", skuList);
        } 
        else if ("warehouse-yc".equals(tableKey)) {
            // 场景 B: 返回拼接字符串，key 为 skuList
            //String joinedSku = String.join(",", skuList);
            // 为了安全，替换掉SKU内部可能存在的逗号
             String joinedSku = skuList.stream()
                  .map(s -> s.replace(",", "")) // 去除内部逗号
                  .collect(Collectors.joining(","));
            resultMap.put("skuList", joinedSku);
        } 
        else {
            // 默认处理，或者抛出异常
            resultMap.put("skus", skuList);
        }

        return resultMap;
    }

    /**
     * 辅助方法：从飞书复杂的 Record 结构中提取 SKU 的 text
     * 结构参考：Record -> fields -> SKU(List) -> Map -> text
     */
    private String extractSkuText(AppTableRecord record) {
        if (record.getFields() == null) return null;

        // 获取 SKU 字段
        Object skuObj = record.getFields().get("SKU");
        
        // 飞书的多行文本或关联字段通常是一个 List<Map> 或 JSON Array
        if (skuObj instanceof List) {
            List<?> list = (List<?>) skuObj;
            if (list.isEmpty()) return null;

            // 取第一个元素（假设一个记录只有一个SKU，如果是多个需要修改此处逻辑）
            Object firstItem = list.get(0);
            
            if (firstItem instanceof Map) {
                Map<?, ?> itemMap = (Map<?, ?>) firstItem;
                Object textVal = itemMap.get("text");
                return textVal != null ? textVal.toString() : null;
            }
        }
        return null;
    }
}