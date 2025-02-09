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

//    for perfomance
    private String instagramMediaId;
    private LocalDateTime analyticsLastUpdated;
    private String analyticsPicture;
    private String analyticsCaption;
    private Integer analyticsLikes;
    private Integer analyticsComments;
    private Integer analyticsSaved;
    private Integer analyticsShared;
    private Integer analyticsAccountsEngaged;
    private Integer analyticsAccountsReached;
    private Double sentimentPositive;
    private Double sentimentNegative;
    private Double sentimentNeutral;
}
