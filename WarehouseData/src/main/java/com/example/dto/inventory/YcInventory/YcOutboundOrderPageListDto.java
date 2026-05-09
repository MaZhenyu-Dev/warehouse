package com.example.dto.inventory.YcInventory;

import lombok.Data;

@Data
public class YcOutboundOrderPageListDto {
    private Integer page;
    private Integer pageSize;

    /** 出库单号，多个逗号分隔 */
    private String outboundOrderNos;

    /** 节点时间类型: orderCreateTime */
    private String timeType;

    /** 开始时间(yyyy-MM-dd HH:mm:ss) */
    private String startTime;

    /** 结束时间(yyyy-MM-dd HH:mm:ss) */
    private String endTime;
}