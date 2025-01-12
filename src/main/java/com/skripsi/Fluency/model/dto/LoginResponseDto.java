package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResponseDto {
    // response ke frontend untuk brand dan influencer login
    private Integer id;
    private String name;
    private String instagramId;
}
