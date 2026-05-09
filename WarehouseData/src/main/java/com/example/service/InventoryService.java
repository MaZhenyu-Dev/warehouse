package com.example.service;

import com.example.common.Result;
import com.example.dto.inventory.YcInventory.OutboundOrderPageListResponse;
import com.example.dto.inventory.YcInventory.YcInventoryQueryDto;
import com.example.dto.inventory.YcInventory.YcOutboundOrderPageListDto;
import com.example.dto.inventory.lc.InventoryResponseDto;
import com.example.dto.inventory.lc.LcOrJfInventoryQueryDto;
import com.example.dto.pojo.InventoryCountDto;

public interface InventoryService {
    /**
     * 获取产品库存信息
     * @param request 查询参数
     * @return 封装好的业务数据
     */
    Result<InventoryResponseDto> getProductInventory(LcOrJfInventoryQueryDto request);

    /**
     * 获取良仓仓库库存数据，支持搜索
     * @return 解析后的库存数据列表
     */
    Object getLCWarehouseData(LcOrJfInventoryQueryDto queryDto);

    /**
     * 查询综合库存
     * @param queryDto 查询参数
     * @return 第三方接口返回的JSON字符串 (或者你可以封装一个通用的Response对象)
     */
    String getYCWarehouseData(YcInventoryQueryDto queryDto);


    // 新增的无参方法
    Result<InventoryCountDto> countInventorySuffix();

    // 【新增】统计库存后缀数量方法
    Result<InventoryCountDto> countYcInventorySuffix();


    /**
     * 分页查询小包出库单列表（返回原始JSON字符串，便于排查问题）
     */
    String getParcelOutboundOrderPageListTest(YcOutboundOrderPageListDto dto);

    /**
     * 分页查询小包出库单列表
     */
    OutboundOrderPageListResponse getParcelOutboundOrderPageList(YcOutboundOrderPageListDto dto);
}
