package com.example.koskeun.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class RegisterResponse {
    private Long id;
    private String email;
    private String fullName;
    private String message;
}
