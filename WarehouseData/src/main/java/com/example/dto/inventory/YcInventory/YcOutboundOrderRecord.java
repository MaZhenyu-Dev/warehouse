package com.example.dto.inventory.YcInventory;

import lombok.Data;

@Data
public class YcOutboundOrderRecord {
    private String whCode;
    private String outboundOrderNo;
    private String thirdOrderNo;
    private Integer status; //状态：0-新建；1-已取面单；2-仓库处理中；3-已出库；4-已取消；5-异常
    private String logisticsChannel;
    private String salesPlatform;
    private String referOrderNo;
    private String platformOrderNo;
    private String logisticsTrackNo;
    private String remark;
    private String orderCreateTime;
    private String interceptTime;
    private String outboundTime;
    private String canceledTime;
    private String exceptionTime;
}
