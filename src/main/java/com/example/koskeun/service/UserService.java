package com.example.koskeun.service;

import com.example.koskeun.dto.request.UserRequest;
import com.example.koskeun.model.User;

public interface UserService {
    User findByEmail(String email);

    User updateUserProfile(String email, UserRequest userRequest);
}