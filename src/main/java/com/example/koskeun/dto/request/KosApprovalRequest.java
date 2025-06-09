package com.example.koskeun.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KosApprovalRequest {
    @NotBlank(message = "Status persetujuan tidak boleh kosong")
    private String approvalStatus;

    private String approvalNotes;

}
