package com.example.dto.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 库存统计结果 DTO
 * 对应关系：
 * ma    -> 马振宇 (-宇)
 * yu    -> 刘瑜琦 (-琦)
 * shuai -> 薄铭帅 (-帅)
 * shou  -> 王丰收 (-收)
 * xu    -> 尹文序 (-序)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCountDto {
    private Integer ma;
    private Integer yu;
    private Integer shuai;
    private Integer shou;
    private Integer xu;
}