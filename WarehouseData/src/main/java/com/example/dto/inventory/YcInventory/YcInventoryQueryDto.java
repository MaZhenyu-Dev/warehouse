package com.example.dto.inventory.YcInventory;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YcInventoryQueryDto implements Serializable {

    /**
     * SKU列表，多个以英文逗号分割
     */
    private String skuList;

    /**
     * 仓库代码列表，多个以英文逗号分割
     */
    private String whCodeList;

    /**
     * 当前页，默认1
     */
    private Integer page;

    /**
     * 每页显示条数，1-100，默认10
     * 注意：如果该字段传 null，Service 层会触发自动轮询逻辑
     */
    private Integer pageSize;

    /**
     * 时间类型，固定：operateTime
     */
    private String timeType = "operateTime";

    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss
     */
    private String startTime;

    /**
     * 结束时间 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;

    /**
     * 库存类型属性 0-正品 1-次品
     */
    private Integer stockType;
}