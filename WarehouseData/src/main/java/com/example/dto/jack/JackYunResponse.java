package com.example.dto.jack;

import lombok.Data;

@Data
public class JackYunResponse<T> {

    private Integer code;
    private String msg;
    private String subCode;
    private JackYunResult<T> result;
}