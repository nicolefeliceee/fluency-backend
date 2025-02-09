package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyLinkDto {
    private String link;
    private String mediaId;
}
