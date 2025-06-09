package com.example.koskeun.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class BookingRequest {

    @NotNull(message = "Tanggal mulai sewa tidak boleh kosong.")
    @Future(message = "Tanggal mulai sewa harus di masa depan.")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Penting untuk binding dari form HTML
    private Date startDate;

    @NotNull(message = "Durasi sewa tidak boleh kosong.")
    @Min(value = 1, message = "Durasi sewa minimal 1 bulan.")
    private Integer durationInMonths;
}