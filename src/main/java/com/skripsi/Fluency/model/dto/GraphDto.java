package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GraphDto {
    private List<String> labels;
    private List<String> data;
}
