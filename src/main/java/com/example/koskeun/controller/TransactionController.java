package com.example.koskeun.controller;

import com.example.koskeun.dto.request.BookingRequest;
import com.example.koskeun.dto.request.PaymentRequest;
import com.example.koskeun.model.Transaction;
import com.example.koskeun.model.User;
import com.example.koskeun.service.AuthService;
import com.example.koskeun.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthService authService;

    // --- ENDPOINTS UNTUK PENYEWA (RENTER) ---

    @PostMapping("/book/{kosId}")
    public String createBooking(@PathVariable("kosId") Long kosId,
            @Valid @ModelAttribute("bookingRequest") BookingRequest request,
            BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            // Mengirim error validasi kembali ke halaman detail
            ra.addFlashAttribute("error",
                    "Data booking tidak valid: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/kos/detail/" + kosId;
        }
        try {
            transactionService.createBooking(kosId, request);
            ra.addFlashAttribute("success", "Permintaan booking berhasil! Mohon tunggu persetujuan pemilik.");
            return "redirect:/transactions/bookings";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal membuat booking: " + e.getMessage());
            return "redirect:/kos/detail/" + kosId;
        }
    }

    @GetMapping("/{transactionId}/pay")
    public String showPaymentPage(@PathVariable("transactionId") Long transactionId, Model model,
            RedirectAttributes ra) {
        try {
            User currentUser = authService.getCurrentUserEntity();

            // PERUBAHAN: Panggil nama method yang benar
            Transaction trx = transactionService.getTransactionByIdForUser(transactionId, currentUser);

            model.addAttribute("transaction", trx);
            model.addAttribute("paymentRequest", new PaymentRequest());
            return "booking-payment"; // Mengarahkan ke view booking-payment.html

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal membuka halaman pembayaran: " + e.getMessage());
            return "redirect:/bookings"; // Redirect jika ada error
        }
    }

    @PostMapping("/{transactionId}/pay")
    public String processPayment(@PathVariable("transactionId") Long transactionId,
            @Valid @ModelAttribute("paymentRequest") PaymentRequest request,
            BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error",
                    "Data pembayaran tidak valid: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/transactions/bookings/" + transactionId; // Kembali ke halaman detail booking
        }
        try {
            transactionService.processPayment(transactionId, request);
            ra.addFlashAttribute("success", "Pembayaran berhasil dikirim! Mohon tunggu konfirmasi.");
            return "redirect:/transactions/bookings";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal melakukan pembayaran: " + e.getMessage());
            return "redirect:/transactions/bookings/" + transactionId;
        }
    }

    // --- ENDPOINTS UNTUK PEMILIK (OWNER) / ADMIN ---

    @PostMapping("/{transactionId}/review")
    public String reviewBooking(@PathVariable("transactionId") Long transactionId,
            @RequestParam("approve") boolean approve,
            RedirectAttributes ra) {
        try {
            transactionService.reviewBooking(transactionId, approve);
            ra.addFlashAttribute("success", "Booking telah ditinjau.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal meninjau booking: " + e.getMessage());
        }
        return "redirect:/transactions"; // Kembali ke daftar transaksi pemilik
    }

    @PostMapping("/{transactionId}/confirm")
    public String confirmPayment(@PathVariable("transactionId") Long transactionId,
            @RequestParam("confirm") boolean confirm,
            RedirectAttributes ra) {
        try {
            transactionService.confirmPayment(transactionId, confirm);
            ra.addFlashAttribute("success", "Pembayaran telah dikonfirmasi.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal mengkonfirmasi pembayaran: " + e.getMessage());
        }
        return "redirect:/transactions";
    }

    // --- ENDPOINT UNTUK MENAMPILKAN DAFTAR TRANSAKSI ---
    /**
     * Menangani GET request untuk menampilkan halaman "Riwayat Booking Saya".
     * Method ini akan dipanggil saat pengguna mengakses URL
     * http://localhost:8080/bookings
     */
    @GetMapping("/bookings")
    public String showMyBookingsPage(Model model) {
        try {
            // 1. Panggil service untuk mendapatkan daftar transaksi/booking.
            // Method getTransactionsForCurrentUser() sudah cerdas, ia akan otomatis
            // memberikan data sesuai role pengguna yang login (dalam kasus ini, PENCARI).
            List<Transaction> bookings = transactionService.getTransactionsForCurrentUser();

            // 2. Tambahkan daftar booking ke dalam model.
            // Nama "bookingList" harus sama persis dengan yang digunakan di th:each pada
            // view.
            model.addAttribute("bookingList", bookings);

        } catch (Exception e) {
            // 3. Tangani jika terjadi error saat mengambil data.
            logger.error("Error saat mengambil data booking pengguna: {}", e.getMessage());
            model.addAttribute("error", "Gagal memuat riwayat booking Anda.");
            // Kirim list kosong agar halaman tidak error saat di-render
            model.addAttribute("bookingList", Collections.emptyList());
        }

        // 4. Kembalikan nama file view HTML untuk dirender.
        // Spring akan mencari file bookings.html di dalam folder templates.
        return "bookings";
    }

    @GetMapping
    public String showTransactionListPage(Model model) {
        try {
            model.addAttribute("transactionList", transactionService.getTransactionsForCurrentUser());
        } catch (Exception e) {
            logger.error("Error saat mengambil daftar transaksi: {}", e.getMessage());
            model.addAttribute("error", "Gagal memuat data transaksi.");
            model.addAttribute("transactionList", Collections.emptyList());
        }
        return "list-transaksi";
    }

    @GetMapping("/detail/{id}")
    public String showTransactionDetailPage(@PathVariable("id") Long transactionId, Model model,
            RedirectAttributes ra) {
        try {
            User currentUser = authService.getCurrentUserEntity();
            Transaction transaction = transactionService.getTransactionByIdForUser(transactionId, currentUser);

            model.addAttribute("transaction", transaction);
            return "detail-transaksi"; // Nama file HTML baru

        } catch (Exception e) {
            logger.error("Gagal mengakses detail transaksi {}: {}", transactionId, e.getMessage());
            ra.addFlashAttribute("error", "Gagal membuka detail transaksi: " + e.getMessage());
            return "redirect:/transactions"; // Kembali ke daftar jika error
        }
    }
}