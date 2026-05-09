package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.example.common.Result;
import com.example.config.inventory.JFConfig;
import com.example.config.inventory.LCConfig;
import com.example.config.inventory.YCConfig;
import com.example.dto.inventory.YcInventory.*;
import com.example.dto.inventory.lc.InventoryResponseDto;
import com.example.dto.inventory.lc.LcOrJfInventoryQueryDto;
import com.example.dto.pojo.InventoryCountDto;
import com.example.service.InventoryService;
import com.example.utils.YcSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {


    private final LCConfig lcConfig; // 注入配置类

    private final JFConfig JfConfig;

    private final YCConfig ycConfig;


    /**
     * 获取良仓仓库库存数据，支持搜索
     *
     * @return
     */
    @Override
    public Result<InventoryResponseDto> getProductInventory(LcOrJfInventoryQueryDto request) {
        try {
            // 1. 使用 FastJson2 将请求对象转为 JSON 字符串
            String paramsJson = JSON.toJSONString(request);

            // 2. 拼接 SOAP XML 字符串
            // 使用 CDATA 包裹 JSON 防止特殊字符破坏 XML 结构
            String soapXml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns1="http://www.example.org/Ec/">
                        <SOAP-ENV:Body>
                            <ns1:callService>
                                <paramsJson><![CDATA[%s]]></paramsJson>
                                <appToken>%s</appToken>
                                <appKey>%s</appKey>
                                <service>getProductInventory</service>
                            </ns1:callService>
                        </SOAP-ENV:Body>
                    </SOAP-ENV:Envelope>
                    """.formatted(paramsJson, lcConfig.getAppToken(), lcConfig.getAppKey());

            //log.info("Requesting remote inventory API...");

            // 3. 使用 Hutool 发送 HTTP POST 请求
            String responseXml = HttpRequest.post(lcConfig.getUrl())
                    .header("Content-Type", "text/xml;charset=UTF-8") // SOAP 1.1 content type
                    .body(soapXml)
                    .timeout(10000) // 设置超时 10秒
                    .execute()
                    .body();

            // 4. 解析返回的 XML
            if (StrUtil.isBlank(responseXml)) {
                return Result.error(500, "远程服务无响应");
            }

            // 技巧：直接截取 <response> 标签中的内容
            // 因为返回的是 JSON 包在 XML 里，直接解析 XML 有时候会因为 JSON 里的特殊字符报错，直接截取字符串最稳妥
            String jsonResultStr = StrUtil.subBetween(responseXml, "<response>", "</response>");

            if (StrUtil.isBlank(jsonResultStr)) {
                log.error("Response XML format error or empty data: {}", responseXml);
                return Result.error(500, "解析远程数据失败");
            }

            // 5. 使用 FastJson2 将提取出的 JSON 字符串转为业务对象
            // trim() 去除可能存在的换行或空格
            InventoryResponseDto responseDto = JSON.parseObject(jsonResultStr.trim(), InventoryResponseDto.class);

            // 6. 判断业务结果
            if ("Success".equalsIgnoreCase(responseDto.getAsk())) {
                return Result.success(responseDto);
            } else {
                return Result.error(500, responseDto.getMessage());
            }

        } catch (Exception e) {
            log.error("库存查询异常", e);
            return Result.error(500, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 新增：统计方法
     */
    @Override
    public Result<InventoryCountDto> countInventorySuffix() {
        try {
            // --- 1. 构造默认请求参数 ---
            LcOrJfInventoryQueryDto defaultQuery = new LcOrJfInventoryQueryDto();

            // 设置你要求的默认值
            defaultQuery.setPageSize(null); // null 触发 fetchAllPages = true
            defaultQuery.setPage(1);
            defaultQuery.setProduct_sku("");
            defaultQuery.setProduct_sku_arr(new ArrayList<>());
            defaultQuery.setWarehouse_code("");
            defaultQuery.setWarehouse_code_arr(new ArrayList<>());
            defaultQuery.setUpdate_start_time("");

            // --- 2. 调用复用方法获取全量数据 ---
            // 注意：getLCWarehouseData 返回的是 Object (实际是 List<Object>)
            Object rawResult = this.getLCWarehouseData(defaultQuery);

            if (rawResult == null) {
                return Result.error(500, "获取数据失败：返回为空");
            }

            // 强转为 List<JSONObject>，因为你的 getLCWarehouseData 内部放的是 JSONObject
            List<JSONObject> dataList = (List<JSONObject>) rawResult;

            // --- 3. 内存统计 ---
            int countMa = 0;    // 马振宇
            int countYu = 0;    // 刘瑜琦
            int countShuai = 0; // 薄铭帅
            int countShou = 0;  // 王丰收
            int countXu = 0;    // 尹文序

            if (CollUtil.isNotEmpty(dataList)) {
                for (JSONObject item : dataList) {
                    // 获取标题
                    String title = item.getString("product_title");

                    if (StrUtil.isBlank(title)) {
                        continue;
                    }

                    // 你的复用方法里已经过滤掉了含“壮”的数据，这里直接统计即可
                    if (title.contains("-宇")) {
                        countMa++;
                    }
                    if (title.contains("-瑜")) {
                        countYu++;
                    }
                    if (title.contains("-帅")) {
                        countShuai++;
                    }
                    if (title.contains("-收")) {
                        countShou++;
                    }
                    if (title.contains("-序")) {
                        countXu++;
                    }
                }
            }

            // --- 4. 封装返回 ---
            InventoryCountDto dto = new InventoryCountDto();
            dto.setMa(countMa);
            dto.setYu(countYu);
            dto.setShuai(countShuai);
            dto.setShou(countShou);
            dto.setXu(countXu);

            return Result.success(dto);

        } catch (Exception e) {
            // 捕获 getLCWarehouseData 中抛出的 RuntimeException
            e.printStackTrace();
            return Result.error(500, "统计失败: " + e.getMessage());
        }
    }


    /**
     * 获取良仓仓库库存数据，支持搜索（轮询）
     *
     * @return
     */
    @Override
    public Object getLCWarehouseData(LcOrJfInventoryQueryDto queryDto) {
        // 最终汇总的所有数据
        List<Object> allDataList = new ArrayList<>();

        // --- 1. 判断是否需要轮询所有数据 ---
        boolean fetchAllPages = (queryDto.getPageSize() == null || queryDto.getPageSize() <= 0);

        // 设置实际使用的 pageSize
        int pageSize = fetchAllPages ? 1000 : queryDto.getPageSize();

        // 如果未设置 page，默认为1
        int currentPage = (queryDto.getPage() == null || queryDto.getPage() <= 0) ? 1 : queryDto.getPage();

        boolean hasNextPage = true;

        try {
            // --- 2. 循环轮询所有页面 ---
            while (hasNextPage) {

                // 2.1 组装参数（使用 Hutool 的 Dict 或直接用 Map）
                Map<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("pageSize", pageSize);
                paramsMap.put("page", currentPage);
                paramsMap.put("product_sku", StrUtil.nullToEmpty(queryDto.getProduct_sku()));
                paramsMap.put("product_sku_arr", CollUtil.isEmpty(queryDto.getProduct_sku_arr())
                        ? new ArrayList<>() : queryDto.getProduct_sku_arr());
                paramsMap.put("warehouse_code", StrUtil.nullToEmpty(queryDto.getWarehouse_code()));
                paramsMap.put("warehouse_code_arr", CollUtil.isEmpty(queryDto.getWarehouse_code_arr())
                        ? new ArrayList<>() : queryDto.getWarehouse_code_arr());
                paramsMap.put("update_start_time", StrUtil.nullToEmpty(queryDto.getUpdate_start_time()));

                // 使用 FastJson2 序列化
                String jsonPayload = JSON.toJSONString(paramsMap);

                // 2.2 组装 XML Request
                String soapRequest = buildLCSoapRequest(jsonPayload);

                // 2.3 发送 HTTP 请求（使用 Hutool）
                String xmlResponse = HttpRequest.post(lcConfig.getUrl())
                        .header("Content-Type", "text/xml; charset=UTF-8")
                        .header("Accept-Charset", "UTF-8")
                        .body(soapRequest)
                        .timeout(30000)
                        .execute()
                        .body();

                // 2.4 解析 XML 获取内部 JSON
                String innerJson = extractContentBetweenTag(xmlResponse, "response");
                if (innerJson == null) {
                    throw new RuntimeException("第三方响应格式错误，未找到response标签");
                }

                // 使用 FastJson2 解析
                JSONObject rootNode = JSON.parseObject(innerJson);

                // 2.5 业务判断
                if ("Success".equals(rootNode.getString("ask"))) {

                    JSONArray dataArray = rootNode.getJSONArray("data");
                    if (dataArray != null && !dataArray.isEmpty()) {

                        // --- 修改开始：增加过滤逻辑 ---
                        List<Object> validData = dataArray.stream()
                                .map(obj -> (JSONObject) obj) // 强转为 JSONObject 以便获取字段
                                .filter(item -> {
                                    // 获取 product_title 字段
                                    String title = item.getString("product_title");

                                    // 过滤逻辑：
                                    // 1. 如果标题为空(null)，通常选择保留（或者根据你的业务去掉）
                                    // 2. 如果标题不为空 且 包含 "壮"，则过滤掉（return false）
                                    if (title != null && title.contains("壮")) {
                                        return false; // 包含“壮”，不要这条数据
                                    }

                                    return true; // 保留数据
                                })
                                .collect(Collectors.toList());
                        allDataList.addAll(validData);
                        //allDataList.addAll(dataArray.toList(Object.class));
                    }

                    // 检查是否继续获取下一页
                    if (fetchAllPages) {
                        if ("true".equalsIgnoreCase(rootNode.getString("nextPage"))) {
                            currentPage++;
                        } else {
                            hasNextPage = false;
                        }
                    } else {
                        hasNextPage = false;
                    }

                } else {
                    String errorMsg = rootNode.getString("message");
                    throw new RuntimeException("第三方API报错: " + StrUtil.blankToDefault(errorMsg, "未知错误"));
                }
            }

            return allDataList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 构建 SOAP 请求体
     */
    private String buildLCSoapRequest(String jsonPayload) {
        return StrUtil.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://www.example.org/Ec/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:callService>\n" +
                        "            <paramsJson><![CDATA[{}]]></paramsJson>\n" +
                        "            <appToken>{}</appToken>\n" +
                        "            <appKey>{}</appKey>\n" +
                        "            <service>getProductInventory</service>\n" +
                        "        </ns1:callService>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>",
                jsonPayload,
                lcConfig.getAppToken(),
                lcConfig.getAppKey()
        );
    }

    // 辅助方法：提取XML标签内容
    private String extractContentBetweenTag(String xml, String tagName) {
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";
        int start = xml.indexOf(openTag);
        int end = xml.lastIndexOf(closeTag);
        if (start != -1 && end != -1) {
            return xml.substring(start + openTag.length(), end);
        }
        return null;
    }
    /**
     * 获取九方仓库库存数据，支持搜索
     *
     * @return
     */
    /*@Override
    public Object getJFWarehouseData(LcOrJfInventoryQueryDto queryDto) {
        // 最终汇总的所有数据
        List<Object> allDataList = new ArrayList<>();

        // --- 1. 判断是否需要轮询所有数据 ---
        boolean fetchAllPages = (queryDto.getPageSize() == null || queryDto.getPageSize() <= 0);

        // 设置实际使用的 pageSize
        int pageSize = fetchAllPages ? 1000 : queryDto.getPageSize();

        // 如果未设置 page，默认为1
        int currentPage = (queryDto.getPage() == null || queryDto.getPage() <= 0) ? 1 : queryDto.getPage();

        boolean hasNextPage = true;

        try {
            // --- 2. 循环轮询所有页面 ---
            while (hasNextPage) {

                // 2.1 组装参数（使用 Hutool 的 Dict 或直接用 Map）
                Map<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("pageSize", pageSize);
                paramsMap.put("page", currentPage);
                paramsMap.put("product_sku", StrUtil.nullToEmpty(queryDto.getProduct_sku()));
                paramsMap.put("product_sku_arr", CollUtil.isEmpty(queryDto.getProduct_sku_arr())
                        ? new ArrayList<>() : queryDto.getProduct_sku_arr());
                paramsMap.put("warehouse_code", StrUtil.nullToEmpty(queryDto.getWarehouse_code()));
                paramsMap.put("warehouse_code_arr", CollUtil.isEmpty(queryDto.getWarehouse_code_arr())
                        ? new ArrayList<>() : queryDto.getWarehouse_code_arr());
                paramsMap.put("update_start_time", StrUtil.nullToEmpty(queryDto.getUpdate_start_time()));

                // 使用 FastJson2 序列化
                String jsonPayload = JSON.toJSONString(paramsMap);

                // 2.2 组装 XML Request
                String soapRequest = buildJFSoapRequest(jsonPayload);

                // 2.3 发送 HTTP 请求（使用 Hutool）
                String xmlResponse = HttpRequest.post(JfConfig.getUrl())
                        .header("Content-Type", "text/xml; charset=UTF-8")
                        .header("Accept-Charset", "UTF-8")
                        .body(soapRequest)
                        .timeout(30000)
                        .execute()
                        .body();

                // 2.4 解析 XML 获取内部 JSON
                String innerJson = extractContentBetweenTag(xmlResponse, "response");
                if (innerJson == null) {
                    throw new RuntimeException("第三方响应格式错误，未找到response标签");
                }

                // 使用 FastJson2 解析
                JSONObject rootNode = JSON.parseObject(innerJson);

                // 2.5 业务判断
                if ("Success".equals(rootNode.getString("ask"))) {

                    JSONArray dataArray = rootNode.getJSONArray("data");
                    if (dataArray != null && !dataArray.isEmpty()) {
                        allDataList.addAll(dataArray.toList(Object.class));
                    }

                    // 检查是否继续获取下一页
                    if (fetchAllPages) {
                        if ("true".equalsIgnoreCase(rootNode.getString("nextPage"))) {
                            currentPage++;
                        } else {
                            hasNextPage = false;
                        }
                    } else {
                        hasNextPage = false;
                    }

                } else {
                    String errorMsg = rootNode.getString("message");
                    throw new RuntimeException("第三方API报错: " + StrUtil.blankToDefault(errorMsg, "未知错误"));
                }
            }

            return allDataList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    *//**
     * 构建 SOAP 请求体
     *//*
    private String buildJFSoapRequest(String jsonPayload) {
        return StrUtil.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://www.example.org/Ec/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:callService>\n" +
                        "            <paramsJson><![CDATA[{}]]></paramsJson>\n" +
                        "            <appToken>{}</appToken>\n" +
                        "            <appKey>{}</appKey>\n" +
                        "            <service>getProductInventory</service>\n" +
                        "        </ns1:callService>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>",
                jsonPayload,
                JfConfig.getAppToken(),
                JfConfig.getAppKey()
        );
    }*/


    /**
     * 获取 YC 仓库数据（综合库存）
     */
    @Override
    public String getYCWarehouseData(YcInventoryQueryDto queryDto) {
        // 1. 初始化默认值
        initQueryDefaults(queryDto);

        // 2. 判断模式：分页查询 OR 获取全部
        boolean fetchAll = (queryDto.getPageSize() == null);

        if (!fetchAll) {
            // === 普通分页模式 ===
            if (queryDto.getPage() == null) queryDto.setPage(1);
            return executeRequest(queryDto);
        } else {
            // === 轮询获取所有数据模式 ===

            // 设置批次大小 (减少HTTP请求次数)
            queryDto.setPageSize(100);
            queryDto.setPage(1);

            // 1. 获取第一页数据
            String firstResponseStr = executeRequest(queryDto);

            // 【改动点】使用 Fastjson2 解析字符串
            JSONObject rootJson = JSONObject.parseObject(firstResponseStr);

            // 检查请求是否成功
            if (rootJson == null || !Integer.valueOf(200).equals(rootJson.getInteger("code"))) {
                return firstResponseStr; // 失败直接返回原串
            }

            JSONObject dataObj = rootJson.getJSONObject("data");
            if (dataObj == null) return firstResponseStr;

            // 获取分页信息
            Integer totalPages = dataObj.getInteger("pages");
            if (totalPages == null) totalPages = 0;

            JSONArray allRecords = dataObj.getJSONArray("records");
            if (allRecords == null) allRecords = new JSONArray();

            // 2. 循环获取剩余页的数据
            if (totalPages > 1) {
                for (int i = 2; i <= totalPages; i++) {
                    // 修改页码
                    queryDto.setPage(i);
                    // 发送请求
                    String subResponseStr = executeRequest(queryDto);

                    // 解析子结果
                    JSONObject subJson = JSONObject.parseObject(subResponseStr);
                    // 安全获取 data
                    JSONObject subData = subJson != null ? subJson.getJSONObject("data") : null;

                    if (subData != null) {
                        JSONArray subRecords = subData.getJSONArray("records");
                        if (subRecords != null) {
                            // Fastjson2 的 JSONArray 支持 addAll
                            allRecords.addAll(subRecords);
                        }
                    }
                }
            }

            // 3. 重组返回结果
            // 更新 records 为合并后的所有数据
            dataObj.put("records", allRecords);

            // 更新分页元数据，伪装成“一页查完”
            dataObj.put("page", 1);
            dataObj.put("pageSize", allRecords.size());
            dataObj.put("total", allRecords.size());
            dataObj.put("pages", 1);

            // 将修改后的 data 放回 root
            rootJson.put("data", dataObj);

            // 【改动点】Fastjson2 序列化为字符串
            return rootJson.toJSONString();
        }
    }

    /**
     * 保持不变的辅助方法：负责签名和发送请求
     * 这里虽然用了 Hutool 发送请求，但返回 String，不影响 JSON 解析库的选择
     */
    private String executeRequest(YcInventoryQueryDto queryDto) {
        // ... (保持之前的逻辑，使用 System.currentTimeMillis, YcSignUtils 等) ...
        String reqTime = String.valueOf(System.currentTimeMillis() / 1000);
        String authCode = YcSignUtil.generateAuthCode(
                ycConfig.getAppKey(), ycConfig.getAppSecret(), reqTime, queryDto
        );

        // 构建有序 Map
        Map<String, Object> dataMap = BeanUtil.beanToMap(queryDto, false, true);
        TreeMap<String, Object> sortedDataMap = MapUtil.sort(dataMap);

        XlwmsBaseRequest<Map<String, Object>> requestBody = XlwmsBaseRequest.<Map<String, Object>>builder()
                .appKey(ycConfig.getAppKey())
                .reqTime(reqTime)
                .data(sortedDataMap)
                .build();

        // 这里序列化请求体依然可以用 Hutool，或者也可以改成 Fastjson2，只要远端能认就行
        // 能跑通，这里可以不用动，或者用 JSON.toJSONString(requestBody)
        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(true);
        String jsonBody = JSONUtil.toJsonStr(requestBody, jsonConfig);

        String url = ycConfig.getInventoryUrl() + "?authcode=" + authCode;

        try {
            return HttpRequest.post(url)
                    .body(jsonBody)
                    .timeout(10000)
                    .execute()
                    .body();
        } catch (Exception e) {
            log.error("Request Error", e);
            throw new RuntimeException("Service Unavailable");
        }
    }

    private void initQueryDefaults(YcInventoryQueryDto queryDto) {
        // ... (保持之前的逻辑) ...
        if (StrUtil.isBlank(queryDto.getTimeType())) queryDto.setTimeType("operateTime");
        if (StrUtil.isBlank(queryDto.getSkuList())) queryDto.setSkuList(null);
        if (StrUtil.isBlank(queryDto.getWhCodeList())) queryDto.setWhCodeList(null);
        if (StrUtil.isBlank(queryDto.getEndTime())) queryDto.setEndTime(DateUtil.now());
        if (StrUtil.isBlank(queryDto.getStartTime())) {
            Date endDate = DateUtil.parse(queryDto.getEndTime());
            queryDto.setStartTime(DateUtil.format(DateUtil.offsetDay(endDate, -60), DatePattern.NORM_DATETIME_PATTERN));
        }
        if (queryDto.getStockType() == null) queryDto.setStockType(null);
    }


    // 2. 新增的统计方法实现
    @Override
    public Result<InventoryCountDto> countYcInventorySuffix() {
        try {
            // --- Step 1: 构造默认参数 ---
            YcInventoryQueryDto queryDto = new YcInventoryQueryDto();
            // 关键点：设置为 null，触发 getYCWarehouseData 内部的轮询全量逻辑
            queryDto.setPageSize(null);
            queryDto.setPage(1);

            // 初始化其他参数 (根据 initQueryDefaults 的逻辑，有些可以不传，利用默认值)
            // 如果有必要，可以在这里手动设置默认时间等

            // --- Step 2: 获取全量数据 (返回的是 JSON 字符串) ---
            String jsonResponseStr = this.getYCWarehouseData(queryDto);

            // --- Step 3: 解析 JSON ---
            if (StrUtil.isBlank(jsonResponseStr)) {
                return Result.error(500, "远程服务返回为空");
            }

            JSONObject rootJson = JSON.parseObject(jsonResponseStr);

            // 校验 code 是否为 200 (注意类型转换)
            if (!Integer.valueOf(200).equals(rootJson.getInteger("code"))) {
                return Result.error(500, "远程报错: " + rootJson.getString("message"));
            }

            // 获取 data -> records
            JSONObject dataObj = rootJson.getJSONObject("data");
            if (dataObj == null) {
                return Result.success(new InventoryCountDto(0, 0, 0, 0, 0));
            }

            JSONArray records = dataObj.getJSONArray("records");

            // --- Step 4: 内存统计 ---
            int countMa = 0;    // 马振宇
            int countYu = 0;    // 刘瑜琦
            int countShuai = 0; // 薄铭帅
            int countShou = 0;  // 王丰收
            int countXu = 0;    // 尹文序

            if (records != null && !records.isEmpty()) {
                for (int i = 0; i < records.size(); i++) {
                    JSONObject item = records.getJSONObject(i);

                    // 【注意】请确认 YC 接口返回的商品名称字段是哪个？
                    // 常见的有: "product_name", "goodsName", "skuName", "product_title"
                    // 这里假设是 "product_name"，如果不对请修改此处字符串
                    String title = item.getString("productName");
                    /*// 如果字段名不确定，可以多试几个:
                    if (title == null) title = item.getString("goods_name");
                    if (title == null) title = item.getString("sku_name");
                    if (title == null) title = item.getString("productName");*/

                    if (StrUtil.isBlank(title)) {
                        continue;
                    }

                    // 1. 过滤逻辑 (保持和之前 LC 的逻辑一致，过滤掉含"壮"的)
                    if (title.contains("壮")) {
                        continue;
                    }

                    // 2. 统计逻辑
                    if (title.contains("-宇")) {
                        countMa++;
                    }
                    if (title.contains("-瑜")) {
                        countYu++;
                    }
                    if (title.contains("-帅")) {
                        countShuai++;
                    }
                    if (title.contains("-收")) {
                        countShou++;
                    }
                    if (title.contains("-序")) {
                        countXu++;
                    }
                }
            }

            // --- Step 5: 封装并返回 ---
            InventoryCountDto dto = new InventoryCountDto();
            dto.setMa(countMa);
            dto.setYu(countYu);
            dto.setShuai(countShuai);
            dto.setShou(countShou);
            dto.setXu(countXu);

            return Result.success(dto);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "统计 YC 库存异常: " + e.getMessage());
        }
    }


    @Override
    public String getParcelOutboundOrderPageListTest(YcOutboundOrderPageListDto dto) {

        if (dto.getPage() == null) dto.setPage(1);
        if (dto.getPageSize() == null) dto.setPageSize(50);
        // timeType 默认值（文档提示：orderCreateTime）
        if (StrUtil.isBlank(dto.getTimeType())) dto.setTimeType("orderCreateTime");

        // outboundOrderNos 空字符串当成 null，避免参与签名/请求
        if (StrUtil.isBlank(dto.getOutboundOrderNos())) dto.setOutboundOrderNos(null);

        return executeOutboundOrderRequestTest(dto);
    }

    private String executeOutboundOrderRequestTest(YcOutboundOrderPageListDto dto) {
        // 关键点1：秒级时间戳（与你库存接口一致）
        String reqTime = String.valueOf(System.currentTimeMillis() / 1000);

        // 关键点2：签名使用 dto（内部会忽略null、排序、json化）
        String authCode = YcSignUtil.generateAuthCode(
                ycConfig.getAppKey(), ycConfig.getAppSecret(), reqTime, dto
        );

        // 关键点3：请求 body 的 data 用有序TreeMap（与你库存接口一致）
        Map<String, Object> dataMap = BeanUtil.beanToMap(dto, false, true);
        TreeMap<String, Object> sortedDataMap = MapUtil.sort(dataMap);

        XlwmsBaseRequest<Map<String, Object>> requestBody = XlwmsBaseRequest.<Map<String, Object>>builder()
                .appKey(ycConfig.getAppKey())
                .reqTime(reqTime)
                .data(sortedDataMap)
                .build();

        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(true);
        String jsonBody = JSONUtil.toJsonStr(requestBody, jsonConfig);

        // 关键点4：authcode 放在 query 上，并且参数名是小写 authcode（与你库存接口一致）
        String url = ycConfig.getOutboundOrderPageListUrl() + "?authcode=" + authCode;

        // 建议把请求打印出来，方便排查
        //log.info("[翼仓] outboundOrderPageList url={}", url);
        //log.info("[翼仓] outboundOrderPageList body={}", jsonBody);

        try {
            String resp = HttpRequest.post(url)
                    .body(jsonBody)
                    .timeout(10000)
                    .execute()
                    .body();

            //log.info("[翼仓] outboundOrderPageList resp={}", resp);
            return resp;
        } catch (Exception e) {
            log.error("[翼仓] outboundOrderPageList Request Error", e);
            throw new RuntimeException("Service Unavailable");
        }
    }


    /**
     * 分页查询小包出库单列表
     *
     * @param dto 查询参数
     * @return 第三方接口返回的JSON字符串 (或者你可以封装一个通用的Response对象)
     */
    @Override
    public OutboundOrderPageListResponse getParcelOutboundOrderPageList(YcOutboundOrderPageListDto dto) {

        if (dto.getPage() == null) dto.setPage(1);
        if (dto.getPageSize() == null) dto.setPageSize(50);
        if (StrUtil.isBlank(dto.getTimeType())) dto.setTimeType("orderCreateTime");
        if (StrUtil.isBlank(dto.getOutboundOrderNos())) dto.setOutboundOrderNos(null);

        return executeOutboundOrderRequest(dto);
    }

    private static final Map<String, String> PLATFORM_MAPPING = Map.of(
            "Amazon", "AMZ",
            "34", "Temu",
            "AliExpress", "AE",
            "Shopee", "SHOPEE",
            "TikTok", "TIKTOK",
            "Other", "OTHER"
    );
    private String mapSalesPlatform(String salesPlatform) {
        if (salesPlatform == null) return "UNKNOWN";
        return PLATFORM_MAPPING.getOrDefault(salesPlatform, "OTHER");
    }

    private OutboundOrderPageListResponse executeOutboundOrderRequest(YcOutboundOrderPageListDto dto) {
        String reqTime = String.valueOf(System.currentTimeMillis() / 1000);

        String authCode = YcSignUtil.generateAuthCode(
                ycConfig.getAppKey(), ycConfig.getAppSecret(), reqTime, dto
        );

        Map<String, Object> dataMap = BeanUtil.beanToMap(dto, false, true);
        TreeMap<String, Object> sortedDataMap = MapUtil.sort(dataMap);

        XlwmsBaseRequest<Map<String, Object>> requestBody = XlwmsBaseRequest.<Map<String, Object>>builder()
                .appKey(ycConfig.getAppKey())
                .reqTime(reqTime)
                .data(sortedDataMap)
                .build();

        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(true);
        String jsonBody = JSONUtil.toJsonStr(requestBody, jsonConfig);

        String url = ycConfig.getOutboundOrderPageListUrl() + "?authcode=" + authCode;

        try {
            String respStr = HttpRequest.post(url)
                    .body(jsonBody)
                    .timeout(10000)
                    .execute()
                    .body();

            // 反序列化为实体（忽略不匹配字段，增强兼容性）
            OutboundOrderPageListResponse resp = JSON.parseObject(
                    respStr,
                    OutboundOrderPageListResponse.class,
                    JSONReader.Feature.SupportSmartMatch
            );

            if (resp != null && resp.getData() != null && resp.getData().getRecords() != null) {
                resp.getData().getRecords().forEach(r -> {
                    r.setSalesPlatform(mapSalesPlatform(r.getSalesPlatform()));
                });
            }

            if (resp == null) {
                throw new IllegalStateException("翼仓响应反序列化失败，响应为空。原始响应：" + respStr);
            }

            // 你也可以选择不抛异常，直接把 resp 返回给上层
            if (resp.getCode() == null || resp.getCode() != 200) {
                throw new IllegalStateException("翼仓接口返回失败：code=" + resp.getCode()
                        + ", msg=" + resp.getMsg());
            }

            return resp;
        } catch (Exception e) {
            log.error("[翼仓] outboundOrderPageList Request Error", e);
            throw new RuntimeException("Service Unavailable", e);
        }
    }
}