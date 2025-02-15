package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.InfluencerFilterRequestDto;
import com.skripsi.Fluency.model.dto.InfluencerFilterResponseDto;
import com.skripsi.Fluency.model.dto.WalletResponseDto;
import com.skripsi.Fluency.service.InfluencerService;
import com.skripsi.Fluency.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("wallet")
public class WalletController {

    @Autowired
    public WalletService walletService;

    @PostMapping("transfer/{user-id}")
    public ResponseEntity<?> transferWallet(@RequestBody Integer amount, @PathVariable(name = "user-id") Integer userId) {
        try {
            System.out.println("ini masuk wallet controller transfer");
            String result = walletService.transferWallet(userId, amount);

            if ("Transfer successful".equals(result)) {
                return ResponseEntity.ok(userId);
            } else if ("Wallet not found".equals(result)) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
                return ResponseEntity.notFound().build() ;
            } else if ("Insufficient balance".equals(result)) {
                return  ResponseEntity.unprocessableEntity().build();
            } else {
                return ResponseEntity.badRequest().build() ;
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("info/{user-id}")
    public ResponseEntity<?> walletInfo(@PathVariable(name = "user-id") Integer userId) {
        try {
            System.out.println("ini masuk wallet controller info");

            WalletResponseDto response = walletService.walletInfo(userId);

            return ResponseEntity.ok(response);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("checkout/{user-id}")
    public ResponseEntity<?> checkout(@RequestBody Integer amount, @PathVariable(name = "user-id") Integer userId) {
        try {
            System.out.println("ini masuk wallet controller transfer");
            String result = walletService.checkout(userId, amount);

            if ("Transfer successful".equals(result)) {
                return ResponseEntity.ok(userId);
            } else if ("Wallet not found".equals(result)) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
                return ResponseEntity.notFound().build() ;
            } else if ("Insufficient balance".equals(result)) {
                return  ResponseEntity.unprocessableEntity().build();
            } else {
                return ResponseEntity.badRequest().build() ;
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
