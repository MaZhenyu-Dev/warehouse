package com.example.dto.jack;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JackYunSkuStockItem {
    //上游结构，不影响对外输出字段
    private BigDecimal yesterdayQuantity;
    private BigDecimal threedayQuantity;
    private BigDecimal weekQuantity;
    private BigDecimal totalSaleQuantity;
    private BigDecimal costPrice;
    private String skuBarcode;
}