package com.skripsi.Fluency.controller;

import com.midtrans.Config;
import com.midtrans.Midtrans;
import com.midtrans.httpclient.CoreApi;
import com.midtrans.httpclient.SnapApi;
import com.midtrans.service.impl.MidtransSnapApiImpl;
import com.skripsi.Fluency.model.dto.PaymentRequestDto;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/midtrans")
public class MidtransController {

    private final Config midtransClient;

    @Value("${midtrans.server.key}")
    private String serverKey;

    public MidtransController(Config midtransClient) {
        this.midtransClient = midtransClient;
    }

    @PostMapping("get-token")
    public ResponseEntity<?> createTransaction(@RequestBody PaymentRequestDto request) {
        try {
            Midtrans.serverKey = serverKey;
            Midtrans.isProduction = false;

            Map<String, Object> params = new HashMap<>();

            Map<String, String> transactionDetails = new HashMap<>();
            transactionDetails.put("order_id", request.getId());
            transactionDetails.put("gross_amount", request.getAmount());

            Map<String, String> creditCard = new HashMap<>();
            creditCard.put("secure", "true");

            params.put("transaction_details", transactionDetails);
            params.put("credit_card", creditCard);

            String response = SnapApi.createTransactionToken(params);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

//    @GetMapping("get-status")
//    public ResponseEntity<?> getStatus(@RequestBody PaymentRequestDto request) {
//        try {
//
//
//            String response = Midtrans;
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
}
