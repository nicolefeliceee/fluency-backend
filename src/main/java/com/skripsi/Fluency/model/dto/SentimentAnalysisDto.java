package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Builder
@Data
public class SentimentAnalysisDto {
    private String sentimentPositive;
    private String sentimentNegative;
    private String sentimentNeutral;
    private List<HashMap<String, String>> topComments;
}
