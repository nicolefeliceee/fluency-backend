package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectDetailDto {
    private String id;
    private String mediatypeId;
    private String note;
    private String deadlineTime;
    private String deadlineDate;
    private String nominal;
    private String link;
    private String statusId;
}
