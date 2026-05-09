package com.example.dto.inventory.YcInventory;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XlwmsBaseRequest<T> {
    private String appKey;
    private String reqTime;
    private T data;
}