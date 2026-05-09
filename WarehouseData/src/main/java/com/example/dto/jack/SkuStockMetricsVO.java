package com.example.dto.jack;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuStockMetricsVO {
    //对外显示字段
    private BigDecimal yesterdayQuantity;
    private BigDecimal threedayQuantity;
    private BigDecimal weekQuantity;
    private BigDecimal totalSaleQuantity;
    private BigDecimal costPrice;
    private String skuBarcode;
}