package com.example.koskeun.config;

import com.example.koskeun.model.Role;
import com.example.koskeun.model.User;
import com.example.koskeun.repository.RoleRepository;
import com.example.koskeun.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        createRoleIfNotFound("admin");
        createRoleIfNotFound("pencari");
        createRoleIfNotFound("pemilik");

        if (userRepository.count() == 0 || userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setFullName("Admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRoleId(1L);
            userRepository.save(admin);
            System.out.println("User admin telah dibuat");
        }
    }

    private void createRoleIfNotFound(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}
