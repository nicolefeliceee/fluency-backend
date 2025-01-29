package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponseDto {
    private Integer id;
    private Integer walletHeaderId;
    private Integer balance;
//    private List<WalletDetailDto> walletDetailDtos;
    private String balanceShow;
    private Map<String, List<WalletDetailDto>> walletDetailsGrouped;
}
