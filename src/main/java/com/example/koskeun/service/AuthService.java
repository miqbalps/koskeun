package com.example.koskeun.service;

import com.example.koskeun.model.Role;
import com.example.koskeun.model.User;
import com.example.koskeun.repository.RoleRepository;
import com.example.koskeun.repository.UserRepository;
import com.example.koskeun.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String email, String password, String fullName, String phoneNumber, String roleName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already taken!");
        }

        // Normalize role name to lowercase to match database values
        String normalizedRoleName = roleName.toLowerCase();
        Role role = roleRepository.findByName(normalizedRoleName)
                .orElseThrow(() -> new RuntimeException("Role '" + normalizedRoleName + "' not found"));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setRoleId(role.getId());
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);

        return userRepository.save(user);
    }

    /**
     * Method BARU dan DIREKOMENDASIKAN.
     * Mengambil entitas User asli dari konteks keamanan.
     * Gunakan method ini di semua service lain (TransactionService, KosService,
     * etc.).
     */
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            // Panggil getter yang sudah kita buat di UserDetailsImpl
            return ((UserDetailsImpl) principal).getUser();
        }

        // Fallback jika terjadi kasus yang tidak terduga
        return null;
    }

    /**
     * Method yang mengembalikan objek Principal dari Spring Security.
     * Berguna untuk otorisasi cepat, tapi jangan gunakan untuk mendapatkan data
     * entitas.
     */
    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }
}
