package com.example.koskeun.security;

import com.example.koskeun.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    // KUNCI: Simpan entitas User asli
    // @JsonIgnore mencegah password terkirim dalam response API jika objek ini
    // ter-serialisasi
    @JsonIgnore
    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    /**
     * Factory method untuk membuat instance UserDetailsImpl dari entitas User.
     */
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(user);
    }

    /**
     * KUNCI: Getter untuk mendapatkan entitas User asli.
     * Ini yang akan digunakan oleh service lain.
     */
    public User getUser() {
        return this.user;
    }

    // --- Delegate Methods to the User Entity ---
    // Semua method di bawah ini sekarang mengambil data langsung dari objek User

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Asumsi user.getRole().getName() mengembalikan "ADMIN", "PEMILIK", dll.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
    }

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Menggunakan email sebagai username
    }

    // --- Getter tambahan yang mungkin berguna di view ---
    public String getFullName() {
        return user.getFullName();
    }

    public String getPhoneNumber() {
        return user.getPhoneNumber();
    }

    public String getGender() {
        return user.getGender();
    }

    public Date getBirthDate() {
        return user.getBirthDate();
    }

    public String getOccupation() {
        return user.getOccupation();
    }

    public String getInstitutionName() {
        return user.getInstitutionName();
    }

    public String getCityOrigin() {
        return user.getCityOrigin();
    }

    public String getMaritalStatus() {
        return user.getMaritalStatus();
    }

    public String getLastEducation() {
        return user.getLastEducation();
    }

    public String getEmergencyContact() {
        return user.getEmergencyContact();
    }

    public String getProfilePhoto() {
        return user.getProfilePhoto();
    }

    public String getIdCardPhoto() {
        return user.getIdCardPhoto();
    }

    public String getRoleName() {
        return user.getRole().getName();
    }

    // --- Metode standar UserDetails ---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(this.user.getId(), that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }
}