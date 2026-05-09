package com.example.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.config.jack.JackYunProperties;
import com.example.dto.jack.*;
import com.example.service.JackYunStockService;
import com.example.utils.JackYunSignUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JackYunStockServiceImpl implements JackYunStockService {

    private static final String METHOD = "erp-stock.stock.skulist";
    private final JackYunProperties props;

    @Override
    public JackYunResponse<SkuStockMetricsVO> querySkuStockMetrics(SkuListQueryRequest request) {
        if (request == null || StrUtil.isBlank(request.getWarehouseCode())) {
            throw new IllegalArgumentException("warehouseCode 为必填");
        }

        // bizcontent（签名用：未URL编码的原始JSON）
        Map<String, Object> biz = new LinkedHashMap<>();
        biz.put("pageIndex", Optional.ofNullable(request.getPageIndex()).orElse(0));
        biz.put("pageSize", Optional.ofNullable(request.getPageSize()).orElse(50));
        biz.put("warehouseCode", request.getWarehouseCode());

        if (StrUtil.isNotBlank(request.getGoodsNo())) biz.put("goodsNo", request.getGoodsNo());
        if (StrUtil.isNotBlank(request.getGoodsName())) biz.put("goodsName", request.getGoodsName());
        if (StrUtil.isNotBlank(request.getSkuName())) biz.put("skuName", request.getSkuName());
        if (StrUtil.isNotBlank(request.getSkuBarcode())) biz.put("skuBarcode", request.getSkuBarcode());
        if (StrUtil.isNotBlank(request.getSkuCode())) biz.put("skuCode", request.getSkuCode());

        // 只要这5个字段
        biz.put("cols", String.join(",", List.of(
                "yesterdayQuantity",
                "threedayQuantity",
                "weekQuantity",
                "totalSaleQuantity",
                "costPrice"
        )));

        String bizcontent = JSON.toJSONString(biz);

        // 公共参数（参与签名）
        Map<String, Object> params = new HashMap<>();
        params.put("method", METHOD);
        params.put("appkey", props.getAppKey());
        params.put("version", props.getVersion());
        params.put("contenttype", props.getContentType());
        params.put("timestamp", DateUtil.now());
        params.put("bizcontent", bizcontent);

        String sign = JackYunSignUtil.sign(params, props.getAppSecret());
        params.put("sign", sign);

        // HTTP POST form
        String body;
        try (HttpResponse resp = HttpRequest.post(props.getApiUrl())
                .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                .charset(StandardCharsets.UTF_8)
                .form(params)
                .timeout(15000)
                .execute()) {

            body = resp.body();
            if (resp.getStatus() < 200 || resp.getStatus() >= 300) {
                throw new RuntimeException("JackYun HTTP错误: " + resp.getStatus() + ", body=" + body);
            }
        }

        // 解析吉客云原始结构
        JackYunResponse<JackYunSkuStockItem> upstream =
                JSON.parseObject(body, new TypeReference<JackYunResponse<JackYunSkuStockItem>>() {});


        if (upstream == null) {
            throw new RuntimeException("JackYun返回为空，body=" + body);
        }
        if (!Objects.equals(upstream.getCode(), 200)) {
            throw new RuntimeException("JackYun业务失败: code=" + upstream.getCode()
                    + ", subCode=" + upstream.getSubCode()
                    + ", msg=" + upstream.getMsg()
                    + ", body=" + body);
        }

        List<SkuStockMetricsVO> data = Optional.ofNullable(upstream.getResult())
                .map(JackYunResult::getData)
                .orElse(Collections.emptyList())
                .stream()
                .map(it -> {
                    SkuStockMetricsVO vo = new SkuStockMetricsVO();
                    vo.setSkuBarcode(it.getSkuBarcode());
                    vo.setYesterdayQuantity(it.getYesterdayQuantity());
                    vo.setThreedayQuantity(it.getThreedayQuantity());
                    vo.setWeekQuantity(it.getWeekQuantity());
                    vo.setTotalSaleQuantity(it.getTotalSaleQuantity());
                    vo.setCostPrice(it.getCostPrice());
                    return vo;
                })
                .collect(Collectors.toList());

        // 组装“你想要的格式”（沿用吉客云结构）
        JackYunResponse<SkuStockMetricsVO> out = new JackYunResponse<>();
        out.setCode(upstream.getCode());
        out.setMsg(upstream.getMsg());
        out.setSubCode(upstream.getSubCode());

        JackYunResult<SkuStockMetricsVO> result = new JackYunResult<>();
        result.setContextId(upstream.getResult() == null ? null : upstream.getResult().getContextId());
        result.setData(data);

        out.setResult(result);
        return out;
    }
}