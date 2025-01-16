package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectDetailDto {
    private String mediatypeId;
    private String note;
    private LocalDateTime deadlineDateTime;
    private String nominal;
    private String link;
}
