package com.example.service;

import com.example.dto.jack.JackYunResponse;
import com.example.dto.jack.SkuListQueryRequest;
import com.example.dto.jack.SkuStockMetricsVO;

public interface JackYunStockService {

    JackYunResponse<SkuStockMetricsVO> querySkuStockMetrics(SkuListQueryRequest request);
}