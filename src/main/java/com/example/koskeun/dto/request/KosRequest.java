package com.example.koskeun.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KosRequest {
    @NotBlank(message = "Nama kos tidak boleh kosong")
    private String name;

    @NotNull(message = "Harga tidak boleh kosong")
    @Positive(message = "Harga harus lebih dari 0")
    private BigDecimal priceMonthly;

    @NotBlank(message = "Deskripsi tidak boleh kosong")
    private String description;

    @NotBlank(message = "Tipe kos tidak boleh kosong")
    private String type;

    @NotBlank(message = "Kategori tidak boleh kosong")
    private String category;

    @NotNull(message = "Jumlah kamar tidak boleh kosong")
    @Positive(message = "Jumlah kamar harus lebih dari 0")
    private Integer roomCount;

    @NotNull(message = "Jumlah kamar tersedia tidak boleh kosong")
    @Positive(message = "Jumlah kamar tersedia harus lebih dari 0")
    private Integer roomAvailable;

    @NotBlank(message = "Alamat tidak boleh kosong")
    private String address;

    @NotNull(message = "Latitude tidak boleh kosong")
    private BigDecimal latitude;

    @NotNull(message = "Longitude tidak boleh kosong")
    private BigDecimal longitude;

    private String status = "available";
}
