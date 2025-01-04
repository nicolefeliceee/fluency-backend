package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BrandDto {
    private Integer id;
    private String email;
    private String name;
}
