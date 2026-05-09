package com.example.service;

import com.example.dto.feishu.BatchUpdateRequest;
import com.example.dto.feishu.SearchRecordRequest;
import com.lark.oapi.service.bitable.v1.model.AppTableFieldForList;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;

import java.util.List;
import java.util.Map;

public interface FeiOperationsService {


    /**
     * 查询所有记录（通用，自动分页）
     *
     * @param tableKey 表标识
     * @param request  查询条件
     * @return 所有记录列表
     */
    List<AppTableRecord> searchAllRecords(String tableKey, SearchRecordRequest request);

    /**
     * 查询字段列表（通用）
     *
     * @param tableKey 表标识
     * @return 字段列表
     */
    List<AppTableFieldForList> listFields(String tableKey);


    /**
     * 批量创建记录（通用）
     *
     * @param tableKey 表标识
     * @param records  记录列表
     * @return 创建成功的记录ID列表
     */
    List<String> batchCreateRecords(String tableKey, List<Map<String, Object>> records);

    /**
     * 批量更新记录（通用）
     *
     * @param tableKey 表标识
     * @param request  更新请求
     * @return 更新成功的记录ID列表
     */
    List<String> batchUpdateRecords(String tableKey, BatchUpdateRequest request);


}