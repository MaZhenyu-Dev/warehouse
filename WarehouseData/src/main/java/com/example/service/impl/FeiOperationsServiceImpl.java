package com.example.service.impl;

import com.example.config.feishu.FeiShuConfigRegistry;
import com.example.config.feishu.FeiShuTableConfig;
import com.example.dto.feishu.BatchUpdateRequest;
import com.example.dto.feishu.SearchRecordRequest;
import com.example.service.FeiOperationsService;
import com.lark.oapi.Client;
import com.lark.oapi.service.bitable.v1.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class FeiOperationsServiceImpl implements FeiOperationsService {
    //已改配置文件读取
    /*// 【配置1】飞书应用的凭证
    // 从配置文件读取
    private static final String APP_ID = "cli_a9b12c3eb9791bd8";
    private static final String APP_SECRET = "c6s12ASXns86YXva3Cim9fzQcjYv4MEJ";

    // 【配置2】多维表格的定位信息
    private static final String BITABLE_APP_TOKEN = "V3BLbAMleaM5s3svZPNc1mPgnHC";
    private static final String TABLE_ID = "tblJOhYcMgEdoBGk";

    private static final String VIEW_ID = "vewObIYyzO";*/

    @Autowired
    private FeiShuConfigRegistry configRegistry;


    // ==================== 批量创建记录 ====================

    @Override
    public List<String> batchCreateRecords(String tableKey, List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        FeiShuTableConfig config = configRegistry.getConfig(tableKey);
        Client client = configRegistry.getClient(tableKey);

        try {
            // 1. 转换为 SDK 格式
            AppTableRecord[] appRecords = records.stream()
                    .map(fields -> AppTableRecord.newBuilder()
                            .fields(fields)
                            .build())
                    .toArray(AppTableRecord[]::new);

            // 2. 构建请求
            BatchCreateAppTableRecordReq req = BatchCreateAppTableRecordReq.newBuilder()
                    .appToken(config.getBitableAppToken())
                    .tableId(config.getTableId())
                    .batchCreateAppTableRecordReqBody(
                            BatchCreateAppTableRecordReqBody.newBuilder()
                                    .records(appRecords)
                                    .build()
                    )
                    .build();

            // 3. 调用 API
            BatchCreateAppTableRecordResp resp = client.bitable().appTableRecord().batchCreate(req);

            // 4. 校验结果
            if (!resp.success()) {
                throw new RuntimeException("批量创建失败: " + resp.getCode() + " - " + resp.getMsg());
            }

            // 5. 返回记录ID
            if (resp.getData() != null && resp.getData().getRecords() != null) {
                return Arrays.stream(resp.getData().getRecords())
                        .map(AppTableRecord::getRecordId)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("批量创建异常: " + e.getMessage());
        }
    }

    // ==================== 批量更新记录 ====================

    @Override
    public List<String> batchUpdateRecords(String tableKey, BatchUpdateRequest request) {
        if (request == null || request.getRecords() == null || request.getRecords().isEmpty()) {
            return new ArrayList<>();
        }

        FeiShuTableConfig config = configRegistry.getConfig(tableKey);
        Client client = configRegistry.getClient(tableKey);

        try {
            // 1. 转换为 SDK 格式
            AppTableRecord[] appRecords = request.getRecords().stream()
                    .map(item -> AppTableRecord.newBuilder()
                            .recordId(item.getRecordId())
                            .fields(item.getFields())
                            .build())
                    .toArray(AppTableRecord[]::new);

            // 2. 构建请求
            BatchUpdateAppTableRecordReq req = BatchUpdateAppTableRecordReq.newBuilder()
                    .appToken(config.getBitableAppToken())
                    .tableId(config.getTableId())
                    .batchUpdateAppTableRecordReqBody(
                            BatchUpdateAppTableRecordReqBody.newBuilder()
                                    .records(appRecords)
                                    .build()
                    )
                    .build();

            // 3. 调用 API
            BatchUpdateAppTableRecordResp resp = client.bitable().appTableRecord().batchUpdate(req);

            // 4. 校验结果
            if (!resp.success()) {
                throw new RuntimeException("批量更新失败: " + resp.getCode() + " - " + resp.getMsg());
            }

            // 5. 返回记录ID
            if (resp.getData() != null && resp.getData().getRecords() != null) {
                return Arrays.stream(resp.getData().getRecords())
                        .map(AppTableRecord::getRecordId)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("批量更新异常: " + e.getMessage());
        }
    }



    // ==================== 查询所有记录（自动分页） ====================

    @Override
    public List<AppTableRecord> searchAllRecords(String tableKey, SearchRecordRequest request) {
        List<AppTableRecord> allRecords = new ArrayList<>();
        String pageToken = null;
        boolean hasMore;

        FeiShuTableConfig config = configRegistry.getConfig(tableKey);
        Client client = configRegistry.getClient(tableKey);

        try {
            do {
                // 1. 构建请求体
                SearchAppTableRecordReqBody reqBody = SearchAppTableRecordReqBody.newBuilder()
                        .viewId(config.getViewId())
                        .fieldNames(request.getFieldNames() == null
                                ? new String[0]
                                : request.getFieldNames().toArray(new String[0]))
                        .automaticFields(request.getAutomaticFields())
                        .build();

                // 2. 构建请求
                SearchAppTableRecordReq req = SearchAppTableRecordReq.newBuilder()
                        .appToken(config.getBitableAppToken())
                        .tableId(config.getTableId())
                        .searchAppTableRecordReqBody(reqBody)
                        .pageSize(500)
                        .pageToken(pageToken)
                        .build();

                // 3. 调用 API
                SearchAppTableRecordResp resp = client.bitable().appTableRecord().search(req);

                // 4. 校验结果
                if (!resp.success()) {
                    throw new RuntimeException("查询失败: " + resp.getCode() + " - " + resp.getMsg());
                }

                // 5. 收集数据
                if (resp.getData() != null && resp.getData().getItems() != null) {
                    allRecords.addAll(Arrays.asList(resp.getData().getItems()));
                }

                // 6. 更新分页状态
                if (resp.getData() != null) {
                    hasMore = Boolean.TRUE.equals(resp.getData().getHasMore());
                    pageToken = resp.getData().getPageToken();
                } else {
                    hasMore = false;
                }

            } while (hasMore);

            return allRecords;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询异常: " + e.getMessage());
        }
    }

    // ==================== 查询字段列表 ====================

    @Override
    public List<AppTableFieldForList> listFields(String tableKey) {
        FeiShuTableConfig config = configRegistry.getConfig(tableKey);
        Client client = configRegistry.getClient(tableKey);

        try {
            ListAppTableFieldReq req = ListAppTableFieldReq.newBuilder()
                    .appToken(config.getBitableAppToken())
                    .tableId(config.getTableId())
                    .pageSize(100)
                    .build();

            ListAppTableFieldResp resp = client.bitable().appTableField().list(req);

            if (!resp.success()) {
                throw new RuntimeException("查询字段失败: " + resp.getCode() + " - " + resp.getMsg());
            }

            if (resp.getData() != null && resp.getData().getItems() != null) {
                return Arrays.asList(resp.getData().getItems());
            }
            return new ArrayList<>();

        } catch (Exception e) {
            throw new RuntimeException("查询字段异常: " + e.getMessage());
        }
    }

}