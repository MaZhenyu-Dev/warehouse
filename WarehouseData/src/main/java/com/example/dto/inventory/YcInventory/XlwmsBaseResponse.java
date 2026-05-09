package com.example.dto.inventory.YcInventory;

import lombok.Data;

@Data
public class XlwmsBaseResponse<T> {
    private Integer code;
    private String msg;
    private T data;
}