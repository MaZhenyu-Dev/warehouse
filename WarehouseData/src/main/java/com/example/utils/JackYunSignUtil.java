package com.example.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

import java.util.Map;
import java.util.TreeMap;

public class JackYunSignUtil {

    /**
     * 生成吉客云签名
     * @param params 所有的业务参数（包含 timestamp, method 等）
     * @param appSecret 应用密钥
     * @return 签名字符串
     */
    public static String sign(Map<String, Object> params, String appSecret) {
        // 1. 使用 TreeMap 进行 ASCII 码排序
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);

        StringBuilder sb = new StringBuilder();

        // 2. 头部拼接 Secret
        sb.append(appSecret);

        // 3. 拼接 Key+Value
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // 排除 sign、token、contextid 以及空值
            if (!"sign".equals(key) && !"token".equals(key) && !"contextid".equals(key) 
                && value != null && StrUtil.isNotBlank(String.valueOf(value))) {
                sb.append(key).append(value);
            }
        }

        // 4. 尾部拼接 Secret
        sb.append(appSecret);

        // 5. 【关键点】整个字符串转小写 (这是吉客云最坑的地方)
        String rawStr = sb.toString().toLowerCase();
        
        // 6. MD5 加密并转小写
        // Hutool 的 md5 默认返回 hex 字符串，这里确保是小写
        return SecureUtil.md5(rawStr).toLowerCase();
    }
}