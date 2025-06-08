package com.example.koskeun.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class TransactionRequest {

    // Sesuai dengan name="paymentType" di form
    private String paymentType;

    // Sesuai dengan name="remainingPayment" di form
    // Walaupun namanya remainingPayment, kita tangkap sebagai jumlah yang dibayar
    // saat ini
    private BigDecimal remainingPayment;

    // Sesuai dengan name="rpProofUrl" di form
    // Kita tangkap sebagai MultipartFile untuk di-upload
    private MultipartFile rpProofUrl;

    // Getter dan Setter jika tidak menggunakan Lombok
    // public String getPaymentType() { ... }
    // public void setPaymentType(String paymentType) { ... }
    // ... dst
}