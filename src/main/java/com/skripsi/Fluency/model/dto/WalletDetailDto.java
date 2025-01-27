package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDetailDto {
    private Integer walletDetailId;
    private Integer partnerId;
    private String partnerName;
    private Integer walletHeaderId;
    private Integer transactionTypeId;
    private String transactionTypeLabel;
    private Integer nominal;
    private String dateTime;
    private String nominalShow;
}
