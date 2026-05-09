package com.example.controller;

import com.example.dto.jack.JackYunResponse;
import com.example.dto.jack.SkuListQueryRequest;
import com.example.dto.jack.SkuStockMetricsVO;
import com.example.service.JackYunStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JackYunStockController {

    @Autowired
    private JackYunStockService stockService;

    @PostMapping("/jackyun/stock/sku-metrics")
    public JackYunResponse<SkuStockMetricsVO> querySkuMetrics(@RequestBody SkuListQueryRequest request) {
        return stockService.querySkuStockMetrics(request);
    }
}