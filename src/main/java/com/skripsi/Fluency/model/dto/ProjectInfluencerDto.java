package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Builder
@Data
public class ProjectInfluencerDto {
    private Integer projectheaderid;
    private Integer projectdetailid;
    private LocalDate fulldate;
    private String day;
    private String date;
    private String brandname;
    private String mediatype;
    private String projecttitle;
}
