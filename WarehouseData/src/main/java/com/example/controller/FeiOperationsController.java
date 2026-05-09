package com.example.controller;

import com.example.common.Result;
import com.example.dto.feishu.BatchCreateRequest;
import com.example.dto.feishu.BatchUpdateRequest;
import com.example.dto.feishu.SearchRecordRequest;
import com.example.service.FeiOperationsService;
import com.example.utils.FeiShuValidationUtil;
import com.lark.oapi.service.bitable.v1.model.AppTableFieldForList;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeiOperationsController {

    @Autowired
    private FeiOperationsService feiShuService;

    // ==================== 通用接口（Map 方式）====================

    /**
     * 通用 - 批量创建记录
     * POST /api/feishu/warehouse/records
     * POST /api/feishu/local/records
     */
    @PostMapping("/{tableKey}/create/records")
    public Result<?> createRecords(
            @PathVariable String tableKey,
            @RequestBody BatchCreateRequest request
    ) {
        // 参数校验
        if (request == null) {
            return Result.error(400, "请求体不能为空");
        }
        Result<?> validationResult = FeiShuValidationUtil.validateCreateRequest(request.getRecords());
        if (validationResult != null) {
            return validationResult;
        }

        try {
            List<String> recordIds = feiShuService.batchCreateRecords(tableKey, request.getRecords());
            return Result.success(recordIds);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 通用 - 批量更新记录
     * PUT /api/feishu/warehouse/records
     * PUT /api/feishu/local/records
     */
    @PostMapping("/{tableKey}/update/records")
    public Result<?> updateRecords(
            @PathVariable String tableKey,
            @RequestBody BatchUpdateRequest request
    ) {
        // 参数校验
        if (request == null) {
            return Result.error(400, "请求体不能为空");
        }
        Result<?> validationResult = FeiShuValidationUtil.validateUpdateRequest(request.getRecords());
        if (validationResult != null) {
            return validationResult;
        }

        try {
            List<String> updatedIds = feiShuService.batchUpdateRecords(tableKey, request);
            return Result.success(updatedIds);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }


    /**
     * 通用 - 查询所有记录
     * POST /api/feishu/warehouse/records/search
     * POST /api/feishu/local/records/search
     */
    @PostMapping("/{tableKey}/records/search")
    public Result<?> searchRecords(
            @PathVariable String tableKey,
            @RequestBody(required = false) SearchRecordRequest request
    ) {
        try {
            if (request == null) {
                request = new SearchRecordRequest();
            }
            List<AppTableRecord> records = feiShuService.searchAllRecords(tableKey, request);
            return Result.success(records);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 通用 - 查询字段列表
     * GET /api/feishu/warehouse/fields
     * GET /api/feishu/local/fields
     */
    @GetMapping("/{tableKey}/fields")
    public Result<?> listFields(@PathVariable String tableKey) {
        try {
            List<AppTableFieldForList> fields = feiShuService.listFields(tableKey);
            return Result.success(fields);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

}
