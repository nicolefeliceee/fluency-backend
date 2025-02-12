package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReviewDto {
    private byte[] profilepicturebyte;
    private String profilepicturetype;
    private String profilepicturename;
    private String name;
    private String date;
    private int rating;
    private String review;

}
