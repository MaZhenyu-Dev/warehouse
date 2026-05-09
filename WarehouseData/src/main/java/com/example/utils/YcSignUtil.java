package com.example.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class YcSignUtil {

    /**
     * 生成签名 AuthCode
     */
    public static String generateAuthCode(String appKey, String appSecret, String reqTime, Object data) {
        // ----------------- 步骤 1: 处理 data -----------------
        // 1. Bean 转 Map (ignoreNullValue=true, 确保空字段不参与签名)
        Map<String, Object> dataMap = BeanUtil.beanToMap(data, false, true);

        // 2. data 内部字段排序 (TreeMap)
        TreeMap<String, Object> sortedDataMap = MapUtil.sort(dataMap);

        // 3. 转 JSON (ignoreNullValue=true)
        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(true);
        String sortedDataJson = JSONUtil.toJsonStr(sortedDataMap, jsonConfig);

        // ----------------- 步骤 2: 三个参数排序拼接 -----------------
        // 使用 TreeMap 确保 Key 按字典序排列 (appKey -> data -> reqTime)
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("appKey", appKey);
        signParams.put("data", sortedDataJson);
        signParams.put("reqTime", reqTime);

        // 拼接 Value
        StringBuilder plainText = new StringBuilder();
        for (String value : signParams.values()) {
            plainText.append(value);
        }

        // log.info("最终签名明文: {}", plainText);

        // ----------------- 步骤 3: 加密 -----------------
        return SecureUtil.hmacSha256(appSecret).digestHex(plainText.toString());
    }
}