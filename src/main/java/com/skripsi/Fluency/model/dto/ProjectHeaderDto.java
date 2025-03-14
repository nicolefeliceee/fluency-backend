package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProjectHeaderDto {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String caption;
    private String mention;
    private String hashtag;
    private String influencerId;
    private String brandId;
    private String statusId;
    private String referenceNumber;
    private List<ProjectDetailDto> projectDetails;
}