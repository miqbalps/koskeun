package com.example.koskeun.controller;

import com.example.koskeun.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String register() {
        return "register"; // Return the registration view
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Return the login view
    }

    @PostMapping("/auth/register")
    public String register(@RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes) {
        try {
            authService.register(email, password, fullName, phoneNumber, role);
            redirectAttributes.addFlashAttribute("registered", true);
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/register";
        }
    }
}
