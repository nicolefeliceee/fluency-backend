package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StoryDetailDto {
    private String id;
    private int sharecount;
    private int viewcount;
    private String mediatype;
    private String mediaproducttype;
    private String mediaurl;
    private String permalink;
    private String timestamp;
    private String thumbnailurl;
}
