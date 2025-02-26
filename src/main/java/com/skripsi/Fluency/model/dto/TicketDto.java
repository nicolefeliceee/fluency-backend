package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketDto {
    private String id;
    private ProjectHeaderDto projectHeader;
    private String reportedDate;
    private String resolvedDate;
    private String status;
}
