package com.example.koskeun.security;

import com.example.koskeun.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String gender;
    private Date birthDate;
    private String occupation;
    private String institutionName;
    private String cityOrigin;
    private String maritalStatus;
    private String lastEducation;
    private String emergencyContact;
    private String profilePhoto;
    private String idCardPhoto;
    private String roleName;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String email, String password, String fullName,
            String phoneNumber, String gender, Date birthDate, String occupation,
            String institutionName, String cityOrigin, String maritalStatus,
            String lastEducation, String emergencyContact, String profilePhoto,
            String idCardPhoto, String roleName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.institutionName = institutionName;
        this.cityOrigin = cityOrigin;
        this.maritalStatus = maritalStatus;
        this.lastEducation = lastEducation;
        this.emergencyContact = emergencyContact;
        this.profilePhoto = profilePhoto;
        this.idCardPhoto = idCardPhoto;
        this.roleName = roleName;
        this.authorities = Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getBirthDate(),
                user.getOccupation(),
                user.getInstitutionName(),
                user.getCityOrigin(),
                user.getMaritalStatus(),
                user.getLastEducation(),
                user.getEmergencyContact(),
                user.getProfilePhoto(),
                user.getIdCardPhoto(),
                user.getRole().getName());
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public String getCityOrigin() {
        return cityOrigin;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public String getLastEducation() {
        return lastEducation;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public String getIdCardPhoto() {
        return idCardPhoto;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

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
}
