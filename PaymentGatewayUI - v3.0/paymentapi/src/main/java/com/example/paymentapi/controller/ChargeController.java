package com.example.paymentapi.controller;

import com.wallee.sdk.ApiClient;
import com.wallee.sdk.service.TransactionService;
import com.wallee.sdk.model.LineItemCreate;
import com.wallee.sdk.model.LineItemType;
import com.wallee.sdk.model.TransactionCreate;
import com.wallee.sdk.model.Transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;

@RestController
@RequestMapping("/charge")
public class ChargeController {

    private final ApiClient client;

    @Value("${wallee.spaceId}")
    private Long spaceId;

    public ChargeController(ApiClient client) {
        this.client = client;
    }

@PostMapping
public String createCharge(@RequestBody PaymentRequest request) {
    try {
        BigDecimal amount = request.getAmount();
        String description = request.getDescription();

        TransactionCreate tx = new TransactionCreate();
        tx.setCurrency("EUR");
        tx.setAutoConfirmationEnabled(true);
        tx.setLineItems(Collections.singletonList(
            new LineItemCreate()
                .name(description)
                .amountIncludingTax(amount)
                .quantity(BigDecimal.ONE)
                .uniqueId("item-" + System.currentTimeMillis())
                .type(LineItemType.PRODUCT)
        ));

        TransactionService txService = new TransactionService(client);
        Transaction transaction = txService.create(spaceId, tx);

        return "Transaction created! ID: " + transaction.getId();
    } catch (Exception e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
    }
  }
}
