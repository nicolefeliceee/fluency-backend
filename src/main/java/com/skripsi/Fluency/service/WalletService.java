package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.dto.WalletDetailDto;
import com.skripsi.Fluency.model.dto.WalletResponseDto;
import com.skripsi.Fluency.model.entity.TransactionType;
import com.skripsi.Fluency.model.entity.WalletDetail;
import com.skripsi.Fluency.model.entity.WalletHeader;
import com.skripsi.Fluency.repository.TransactionTypeRepository;
import com.skripsi.Fluency.repository.WalletDetailRepository;
import com.skripsi.Fluency.repository.WalletHeaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WalletService {

    @Autowired
    public WalletHeaderRepository walletHeaderRepository;

    @Autowired
    public WalletDetailRepository walletDetailRepository;

    @Autowired
    public TransactionTypeRepository transactionTypeRepository;

    public String transferWallet(Integer userId, Integer amount) {
        WalletHeader walletHeader = walletHeaderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

        // Cek saldo mencukupi
        if (walletHeader.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Kurangi saldo
        System.out.println("saldo saat ini: " + walletHeader.getBalance());
        System.out.println("amount di tf: " + amount);
        walletHeader.setBalance(walletHeader.getBalance() - amount);
        walletHeaderRepository.save(walletHeader);
        System.out.println("saldo after: " + walletHeader.getBalance());

        // Ambil TransactionType untuk transfer (ID = 1)
        TransactionType transactionType = transactionTypeRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Transaction type 'Transfer' not found"));


        // Buat WalletDetail baru
        WalletDetail walletDetail = new WalletDetail();
        walletDetail.setWalletHeader(walletHeader);
        walletDetail.setNominal(amount); // Nominal sebagai Integer
        walletDetail.setTransactionType(transactionType); // Contoh TransactionType
        walletDetail.setDateTime(LocalDateTime.now());
        walletDetailRepository.save(walletDetail);

        return "Transfer successful";
    }

    public static String formatPrice(String price) {
//        System.out.println("ini udah masuk di formatprice");
        if (price.isEmpty()) {
            return "";
        }
        // Format angka dengan titik (locale Indonesia)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
        return formatter.format(Long.parseLong(price));
    }

    public static String formatDate(LocalDateTime dateTimeInput) {
        // Input string
        String dateTimeString = String.valueOf(dateTimeInput);

        // Parsing string ke LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);

        // Format output
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH);

        // Format ke string yang diinginkan
        String formattedDate = dateTime.format(formatter);

        // Output hasil
        System.out.println(formattedDate);
        return formattedDate;
    }

    public WalletResponseDto walletInfo(Integer userId){
        WalletHeader walletHeader = walletHeaderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

        // Ambil semua WalletDetail dari WalletHeader
        List<WalletDetail> walletDetails = walletHeader.getWalletDetails();


        // Urutkan walletDetails berdasarkan tanggal terbaru
        walletDetails.sort((detail1, detail2) -> detail2.getDateTime().compareTo(detail1.getDateTime()));

        String plus = "Rp. ";
        String minus = "- Rp. ";

        // Konversi WalletDetail ke WalletDetailDto
        List<WalletDetailDto> walletDetailDtos = new ArrayList<>();
        for (WalletDetail detail : walletDetails) {
            WalletDetailDto dto = new WalletDetailDto();

            String nominalConcat;

            if (detail.getTransactionType().getId() == 1 || detail.getTransactionType().getId() == 2) {
                nominalConcat = (minus.concat(formatPrice(String.valueOf(detail.getNominal()))));
            }
            else {
                nominalConcat = (plus.concat(formatPrice(String.valueOf(detail.getNominal()))));
            }

            dto.setWalletDetailId(detail.getId());
            dto.setPartnerId(detail.getUser() != null ? detail.getUser().getId() : null);
            dto.setPartnerName(detail.getUser() != null ? detail.getUser().getName() : null);
            dto.setWalletHeaderId(detail.getWalletHeader().getId());
            dto.setTransactionTypeId(detail.getTransactionType().getId());
            dto.setTransactionTypeLabel(detail.getTransactionType().getLabel());
            dto.setNominal(detail.getNominal());
            dto.setDateTime(formatDate(detail.getDateTime()));
            dto.setNominalShow(nominalConcat);
            walletDetailDtos.add(dto);
        }

        // Map untuk mengelompokkan data berdasarkan tanggal
        Map<String, List<WalletDetailDto>> groupedByDate = new HashMap<>();

        // Proses grouping berdasarkan tanggal
        for (WalletDetail detail : walletDetails) {
            // Gunakan LocalDateTime untuk grouping
            String formattedDate = formatDate(detail.getDateTime()); // Format untuk keperluan tampilan

            // Tambahkan ke grup berdasarkan tanggal
            groupedByDate.computeIfAbsent(formattedDate, k -> new ArrayList<>())
                    .add(walletDetailDtos.stream()
                            .filter(dto -> dto.getWalletDetailId().equals(detail.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Detail not found")));
        }

        // Bangun response DTO
        WalletResponseDto response = new WalletResponseDto();
        response.setId(walletHeader.getUser().getId());
        response.setWalletHeaderId(walletHeader.getId());
        response.setBalance(walletHeader.getBalance());
        response.setBalanceShow(plus.concat(formatPrice(String.valueOf(walletHeader.getBalance()))));
        response.setWalletDetailsGrouped(groupedByDate);

        return response;
    }

    public String checkout(Integer userId, Integer amount) {
        WalletHeader walletHeader = walletHeaderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

        // Cek saldo mencukupi
        if (walletHeader.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Kurangi saldo
        walletHeader.setBalance(walletHeader.getBalance() - amount);
        walletHeaderRepository.save(walletHeader);

        // Ambil TransactionType untuk pay influencer (ID = 2)
        TransactionType transactionType = transactionTypeRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Transaction type 'Transfer' not found"));


        // Buat WalletDetail baru
        WalletDetail walletDetail = new WalletDetail();
        walletDetail.setWalletHeader(walletHeader);
        walletDetail.setNominal(amount); // Nominal sebagai Integer
        walletDetail.setTransactionType(transactionType); // Contoh TransactionType
        walletDetail.setDateTime(LocalDateTime.now());
        walletDetailRepository.save(walletDetail);

        return "Transfer successful";
    }

    public String topupWallet(Integer userId, Integer amount) {
        WalletHeader walletHeader = walletHeaderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

        // Kurangi saldo
        System.out.println("saldo saat ini: " + walletHeader.getBalance());
        System.out.println("amount di topup: " + amount);
        walletHeader.setBalance(walletHeader.getBalance() + amount);
        walletHeaderRepository.save(walletHeader);
        System.out.println("saldo after: " + walletHeader.getBalance());

        // Ambil TransactionType untuk topup (ID = 3)
        TransactionType transactionType = transactionTypeRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Transaction type 'Transfer' not found"));

        // Buat WalletDetail baru
        WalletDetail walletDetail = new WalletDetail();
        walletDetail.setWalletHeader(walletHeader);
        walletDetail.setNominal(amount); // Nominal sebagai Integer
        walletDetail.setTransactionType(transactionType); // Contoh TransactionType
        walletDetail.setDateTime(LocalDateTime.now());
        walletDetailRepository.save(walletDetail);

        return "Top Up successful";
    }
}
