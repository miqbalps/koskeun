package com.example.koskeun.service;

import com.example.koskeun.dto.request.UserRequest;
import com.example.koskeun.model.User;
import com.example.koskeun.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User updateUserProfile(String email, UserRequest userDto) {
        User user = findByEmail(email);

        user.setFullName(userDto.getFullName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setGender(userDto.getGender());
        user.setBirthDate(userDto.getBirthDate());
        user.setOccupation(userDto.getOccupation());
        user.setInstitutionName(userDto.getInstitutionName());
        user.setCityOrigin(userDto.getCityOrigin());
        user.setMaritalStatus(userDto.getMaritalStatus());
        user.setLastEducation(userDto.getLastEducation());
        user.setEmergencyContact(userDto.getEmergencyContact());

        return userRepository.save(user);
    }
}