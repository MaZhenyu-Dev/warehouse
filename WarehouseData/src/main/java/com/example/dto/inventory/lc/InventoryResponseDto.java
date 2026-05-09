package com.example.dto.inventory.lc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDto {
    private String ask;
    private String message;
    private Pagination pagination;
    private String nextPage;
    private String count;
    private List<InventoryItem> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private String page;
        private String pageSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItem {
        @JsonProperty("product_barcode")
        private String productBarcode;

        @JsonProperty("product_sku")
        private String productSku;

        @JsonProperty("reference_no")
        private String referenceNo;

        @JsonProperty("product_title")
        private String productTitle;

        @JsonProperty("product_title_en")
        private String productTitleEn;

        @JsonProperty("warehouse_code")
        private String warehouseCode;

        private String onway;
        private String pending;
        private String sellable;
        private String unsellable;
        private String reserved;
        private String shipped;

        @JsonProperty("sold_shared")
        private String soldShared;
        private String shared;
        private String warning;

        @JsonProperty("pi_update_time")
        private String piUpdateTime;

        @JsonProperty("warehouse_desc")
        private String warehouseDesc;

        @JsonProperty("batch_info")
        private List<BatchInfo> batchInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchInfo {
        @JsonProperty("receiving_code")
        private String receivingCode;

        @JsonProperty("ib_quantity")
        private String ibQuantity;

        @JsonProperty("ib_type")
        private String ibType;

        @JsonProperty("ib_status")
        private String ibStatus;

        @JsonProperty("ib_fifo_time")
        private String ibFifoTime;

        @JsonProperty("lc_code")
        private String lcCode;

        @JsonProperty("reserved_quantity")
        private String reservedQuantity;

        @JsonProperty("sellable_quantity")
        private Integer sellableQuantity;

        @JsonProperty("stock_age")
        private Integer stockAge;
    }
}