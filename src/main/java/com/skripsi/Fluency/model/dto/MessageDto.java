package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageDto {
    private Integer id;
    private Integer senderId;
    private Integer chatId;
    private String text;
    private LocalDateTime dateTime;
    private String messageType;
    private String url;
}
