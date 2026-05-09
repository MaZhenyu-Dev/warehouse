package com.example.dto.inventory.YcInventory;

import lombok.Data;
import java.util.List;

@Data
public class XlwmsPageResult<T> {

    private Integer total;
    private Integer pageSize;
    private Integer page;

    private List<T> records;
}