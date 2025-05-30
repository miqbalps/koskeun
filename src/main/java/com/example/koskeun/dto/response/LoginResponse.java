package com.example.koskeun.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private String email;
    private String fullName;
    private Collection<? extends GrantedAuthority> roles;
    private String message;
}
