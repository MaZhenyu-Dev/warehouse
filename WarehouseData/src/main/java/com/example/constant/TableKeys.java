package com.example.constant;

/**
 * 飞书表标识常量
 * 避免硬编码字符串
 */
public final class TableKeys {

    public static final String LC_WAREHOUSE = "warehouse-lc";  // 远端仓库
    public static final String LOCAL = "local";          // 本地数据库
    public static final String YC_WAREHOUSE = "warehouse-yc";
    // 新增表时加一行：public static final String ORDER = "order";

    private TableKeys() {
        // 私有构造，防止实例化
    }
}