package com.example.koskeun.controller;

import com.example.koskeun.dto.request.BankRequest;
import com.example.koskeun.model.Bank;
import com.example.koskeun.security.UserDetailsImpl;
import com.example.koskeun.service.BankService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/banks")
public class BankController {

    @Autowired
    private BankService bankService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Bank> getBank(@PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Bank bank = bankService.getBankById(id, userDetails.getId());
            return ResponseEntity.ok(bank);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addBank(@Valid @ModelAttribute BankRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Bank bank = bankService.addBank(request, userDetails.getId());
            return ResponseEntity.ok(bank);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateBank(@PathVariable Long id,
            @Valid @ModelAttribute BankRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Bank bank = bankService.updateBank(id, request, userDetails.getId());
            return ResponseEntity.ok(bank);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBank(@PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            bankService.deleteBank(id, userDetails.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}