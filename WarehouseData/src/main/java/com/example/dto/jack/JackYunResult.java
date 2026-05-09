package com.example.dto.jack;

import lombok.Data;

import java.util.List;

@Data
public class JackYunResult<T> {

    private List<T> data;
    private String contextId;
}