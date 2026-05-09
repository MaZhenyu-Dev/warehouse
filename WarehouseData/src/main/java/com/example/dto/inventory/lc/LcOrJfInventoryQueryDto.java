package com.example.dto.inventory.lc;

import lombok.Data;

import java.util.List;

@Data
public class LcOrJfInventoryQueryDto {
    private Integer pageSize;
    private Integer page;
    private String product_sku;
    private List<String> product_sku_arr;
    private String warehouse_code;
    private List<String> warehouse_code_arr;
    private String update_start_time;
}