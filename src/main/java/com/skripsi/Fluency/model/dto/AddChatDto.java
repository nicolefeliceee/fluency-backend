package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddChatDto {
    private Integer id;
    private String user1Name;
    private String user2Name;
    private Integer user1Id;
    private Integer user2Id;
}
