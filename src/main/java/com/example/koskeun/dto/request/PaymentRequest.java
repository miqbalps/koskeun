package com.example.koskeun.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotBlank(message = "Tipe pembayaran tidak boleh kosong.")
    private String paymentType; // "down_payment" atau "full_payment"

    // PERUBAHAN: Anotasi @NotNull dihapus.
    // Jumlah pembayaran sekarang opsional karena bisa jadi tidak ada untuk
    // 'full_payment'.
    private BigDecimal paymentAmount;

    @NotNull(message = "Bukti pembayaran harus di-upload.")
    private MultipartFile paymentProof;
}