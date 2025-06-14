package com.example.koskeun.service;

import com.example.koskeun.dto.request.BankRequest;
import com.example.koskeun.model.Bank;
import com.example.koskeun.repository.BankRepository;
import com.example.koskeun.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BankService {

    @Autowired
    private BankRepository bankRepository;

    public List<Bank> getBanksByUserId(Long userId) {
        return bankRepository.findByUserId(userId);
    }

    @Transactional
    public Bank addBank(BankRequest request, Long userId) {
        // Check if bank account already exists
        if (bankRepository.existsByUserIdAndBankNameAndAccountNumber(
                userId, request.getBankName(), request.getAccountNumber())) {
            throw new RuntimeException("Rekening bank sudah terdaftar");
        }

        Bank bank = new Bank();
        bank.setUserId(userId);
        bank.setBankName(request.getBankName());
        bank.setAccountNumber(request.getAccountNumber());
        bank.setAccountHolderName(request.getAccountHolderName());

        return bankRepository.save(bank);
    }

    @Transactional
    public Bank updateBank(Long id, BankRequest request, Long userId) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank tidak ditemukan"));

        // Verify ownership
        if (!bank.getUserId().equals(userId)) {
            throw new RuntimeException("Tidak memiliki akses");
        }

        bank.setBankName(request.getBankName());
        bank.setAccountNumber(request.getAccountNumber());
        bank.setAccountHolderName(request.getAccountHolderName());

        return bankRepository.save(bank);
    }

    @Transactional
    public void deleteBank(Long id, Long userId) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank tidak ditemukan"));

        // Verify ownership
        if (!bank.getUserId().equals(userId)) {
            throw new RuntimeException("Tidak memiliki akses");
        }

        bankRepository.delete(bank);
    }

    public Bank getBankById(Long id, Long userId) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank tidak ditemukan"));

        // Verify ownership
        if (!bank.getUserId().equals(userId)) {
            throw new RuntimeException("Tidak memiliki akses");
        }

        return bank;
    }
}