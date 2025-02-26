package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {
    private Integer id;
    private Integer influencerid;
    private Integer brandid;
    private Integer number;
    private String name;
    private String email;
    private String type;
    private String phone;
    private String status;
}
