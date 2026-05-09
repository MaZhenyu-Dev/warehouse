package com.example.utils;

import com.example.common.Result;

import java.util.List;

/**
 * 飞书请求校验工具类
 */
public class FeiShuValidationUtil {

    /**
     * 飞书批量操作上限
     */
    public static final int MAX_BATCH_SIZE = 1000;

    /**
     * 校验批量创建请求
     *
     * @param records 记录列表
     * @return 校验失败返回 Result，通过返回 null
     */
    public static Result<?> validateCreateRequest(List<?> records) {
        if (records == null || records.isEmpty()) {
            return Result.error(400, "数据不能为空");
        }
        if (records.size() > MAX_BATCH_SIZE) {
            return Result.error(400, "单次导入不能超过" + MAX_BATCH_SIZE + "条");
        }
        return null; // 校验通过
    }

    /**
     * 校验批量更新请求
     *
     * @param records 记录列表
     * @return 校验失败返回 Result，通过返回 null
     */
    public static Result<?> validateUpdateRequest(List<?> records) {
        if (records == null || records.isEmpty()) {
            return Result.error(400, "更新列表不能为空");
        }
        if (records.size() > MAX_BATCH_SIZE) {
            return Result.error(400, "单次更新不能超过" + MAX_BATCH_SIZE + "条记录");
        }
        return null; // 校验通过
    }

    private FeiShuValidationUtil() {
        // 工具类私有构造
    }
}