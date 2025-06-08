package com.example.koskeun.controller;

import com.example.koskeun.dto.request.TransactionRequest;
// PERHATIAN: Pastikan model User tidak lagi di-import jika tidak digunakan di tempat lain
// import com.example.koskeun.model.User; 
import com.example.koskeun.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
// PERHATIAN: Import untuk @AuthenticationPrincipal tidak lagi diperlukan
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/rent/{kosId}")
    public String processRent(
            @PathVariable("kosId") Long kosId,
            @ModelAttribute TransactionRequest transactionRequest,
            // PERUBAHAN: Parameter @AuthenticationPrincipal User currentUser dihapus.
            // Service akan mengambil user secara internal.
            RedirectAttributes redirectAttributes) {
        try {
            // PERUBAHAN: Pemanggilan service tidak lagi menyertakan currentUser.
            transactionService.createRentTransaction(kosId, transactionRequest);

            redirectAttributes.addFlashAttribute("success",
                    "Permintaan sewa berhasil dikirim dan sedang menunggu konfirmasi.");
            return "redirect:/my-bookings"; // Redirect ke halaman riwayat booking

        } catch (Exception e) {
            // Sebaiknya gunakan logger di aplikasi sesungguhnya
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal mengirim permintaan: " + e.getMessage());
            return "redirect:/kos/detail/" + kosId; // Redirect kembali ke detail kos jika gagal
        }
    }
}