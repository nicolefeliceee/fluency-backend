package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MediaDetailDto {
    private String id;
    private int likecount;
    private int commentcount;
    private int sharecount;
    private int savecount;
    private String mediatype;
    private String mediaproducttype;
    private String mediaurl;
    private String permalink;
    private String timestamp;
    private String engagement;
    private String thumbnailurl;
}
