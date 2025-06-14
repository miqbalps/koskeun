package com.example.koskeun.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BankRequest {
    @NotBlank(message = "Nama bank tidak boleh kosong")
    private String bankName;

    @NotBlank(message = "Nomor rekening tidak boleh kosong")
    private String accountNumber;

    @NotBlank(message = "Nama pemilik rekening tidak boleh kosong")
    private String accountHolderName;
}