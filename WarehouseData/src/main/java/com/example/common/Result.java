package com.example.common;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"code", "message", "success", "data"})
public class Result<T> {
    private Integer code;       // 状态码，例如 200 成功，500 失败
    private String message;     // 提示信息
    private T data;             // 具体数据

    // 私有构造，强制使用静态方法
    private Result() {}

    // 成功时的响应
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 失败时的响应
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}