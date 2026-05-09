package com.example.controller;

import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.dto.inventory.YcInventory.OutboundOrderPageListResponse;
import com.example.dto.inventory.YcInventory.YcInventoryQueryDto;
import com.example.dto.inventory.YcInventory.YcOutboundOrderPageListDto;
import com.example.dto.inventory.lc.InventoryResponseDto;
import com.example.dto.inventory.lc.LcOrJfInventoryQueryDto;
import com.example.dto.pojo.InventoryCountDto;
import com.example.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    /**
     * 获取良仓仓库库存数据
     *
     * @return 库存数据列表
     */
    @PostMapping("/getLCWarehouseData")
    public Result<InventoryResponseDto> queryInventory(@RequestBody @Validated LcOrJfInventoryQueryDto request) {
        // 简单校验必填项，也可以在 DTO 字段上加 @NotNull
        if (request.getPage() == null || request.getPageSize() == null) {
            return Result.error(400, "page 和 pageSize 不能为空");
        }

        return inventoryService.getProductInventory(request);
    }

    /**
     * 获取良仓仓库库存数据（支持分页轮询）
     *
     * @return 库存数据列表
     */
    @PostMapping("/getLCWarehouseDataAll")
    public Result<Object> getLCWarehouseData(@RequestBody LcOrJfInventoryQueryDto queryDto) {
        try {
            // 调用 Service，传入前端参数
            Object data = inventoryService.getLCWarehouseData(queryDto);
            // 成功返回
            return Result.success(data);

        } catch (Exception e) {
            e.printStackTrace(); // 生产环境建议用 log.error("...", e);
            return Result.error(500, "获取库存失败: " + e.getMessage());
        }
    }

    /**
     * 获取良仓特定后缀的库存统计
     * 请求方式：GET
     * 无需参数
     */
    @GetMapping("/getLCWarehouseDataCount")
    public Result<InventoryCountDto> getInventoryCount() {
        return inventoryService.countInventorySuffix();
    }


    /**
     * 获取九方仓库库存数据
     *
     * @return 仓库库存数据列表
     */
    /*@PostMapping("/getJFWarehouseData")
    public Result<Object> getJFWarehouseData(@RequestBody LcOrJfInventoryQueryDto queryDto) {
        try {
            // 调用 Service，获取库存数据
            Object data = inventoryService.getJFWarehouseData(queryDto);

            // 成功返回：封装 data
            return Result.success(data);

        } catch (Exception e) {
            e.printStackTrace(); // 实际生产中请使用 log.error
            // 失败返回：封装错误信息
            return Result.error(500, "获取库存失败: " + e.getMessage());
        }
    }*/



    /**
     * 获取翼仓仓库数据综合库存
     *
     * @return 仓库库存数据列表
     */
    @PostMapping("/getYCWarehouseData")
    public String getYCWarehouseData(@RequestBody @Validated YcInventoryQueryDto queryDto) {
        if (StrUtil.isBlank(queryDto.getStartTime())) queryDto.setStartTime("2025-01-01 00:00:00");
        try {
            return inventoryService.getYCWarehouseData(queryDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 统计 YC 仓库特定后缀的库存数量
     * GET 无参
     */
    @GetMapping("/getYCWarehouseDataCount")
    public Result<InventoryCountDto> getYcInventoryCount() {
        return inventoryService.countYcInventorySuffix();
    }


    /**
     * 分页查询小包出库单列表测试
     * POST
     */
    @PostMapping("/yc/outboundOrder/pageListTest")
    public String outboundOrderPageListTest(@RequestBody YcOutboundOrderPageListDto dto) {
        return inventoryService.getParcelOutboundOrderPageListTest(dto);
    }

    /**
     * 分页查询小包出库单列表
     * POST
     */
    @PostMapping("/yc/outboundOrder/pageList")
    public OutboundOrderPageListResponse outboundOrderPageList(@RequestBody YcOutboundOrderPageListDto dto) {
        return inventoryService.getParcelOutboundOrderPageList(dto);
    }
}
