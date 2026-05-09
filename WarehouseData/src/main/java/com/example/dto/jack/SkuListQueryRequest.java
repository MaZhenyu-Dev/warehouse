package com.example.dto.jack;

import lombok.Data;

@Data
public class SkuListQueryRequest {

    /** 必填：页码，从0开始 */
    private Integer pageIndex = 0;

    /** 必填：每页记录数 */
    private Integer pageSize = 50;

    /** 必填：仓库编码 */
    private String warehouseCode;

    /** 选填：货品编码 */
    private String goodsNo;

    /** 选填：货品名称 */
    private String goodsName;

    /** 选填：规格 */
    private String skuName;

    /** 选填：条码（支持多条码查询 例:123,456） */
    private String skuBarcode;

    /** 选填：规格编码(外部编码) */
    private String skuCode;
}