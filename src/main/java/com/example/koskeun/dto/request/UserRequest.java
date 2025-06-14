package com.example.koskeun.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class UserRequest {
    private String fullName;
    private String phoneNumber;
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    private String occupation;
    private String institutionName;
    private String cityOrigin;
    private String maritalStatus;
    private String lastEducation;
    private String emergencyContact;
}