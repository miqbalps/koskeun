package com.example.koskeun.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import com.example.koskeun.dto.request.TransactionRequest;
import com.example.koskeun.model.Kos;
import com.example.koskeun.model.Transaction;
import com.example.koskeun.repository.KosRepository;
import com.example.koskeun.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    // private static final Logger logger =
    // LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KosRepository kosRepository;

    @Autowired
    private AuthService authService; // Digunakan untuk otorisasi

    // Path upload didefinisikan langsung seperti pada KosService
    private final Path proofStorageLocation = Paths.get("uploads/payment-proofs");

    // Konstanta untuk validasi file
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");

    public TransactionService() {
        try {
            // Membuat direktori jika belum ada saat service diinisialisasi
            Files.createDirectories(proofStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload bukti pembayaran", e);
        }
    }

    /**
     * Method utama untuk membuat transaksi sewa baru.
     */
    @Transactional
    public Transaction createRentTransaction(Long kosId, TransactionRequest request) {
        // 1. Otorisasi: Pastikan user sudah login
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User harus login untuk melakukan transaksi");
        }

        // 2. Validasi Input DTO
        validateTransactionRequest(request);

        // logger.debug("Starting transaction creation for kosId: {}", kosId);
        // logger.debug("Transaction request: {}", request);
        // 3. Ambil data Kos
        Kos kos = kosRepository.findById(kosId)
                .orElseThrow(() -> new EntityNotFoundException("Kos dengan ID " + kosId + " tidak ditemukan."));

        // 4. Proses dan simpan file bukti pembayaran
        String uniqueFileName = storeProofFile(request.getRpProofUrl());

        try {
            // 5. Buat dan simpan entitas Transaction
            Transaction transaction = new Transaction();
            transaction.setKosId(kos.getId());
            transaction.setUserId(currentUser.getId());
            transaction.setOwnerId(kos.getOwnerId());
            transaction.setPaymentType(request.getPaymentType());
            transaction.setTotal(kos.getPriceMonthly());
            transaction.setRemainingPayment(request.getRemainingPayment());
            transaction.setRpProofUrl(uniqueFileName); // Simpan nama file ke DB
            transaction.setStatus("pending");

            // debug
            System.out.println("Menyimpan transaksi: " + transaction);

            return transactionRepository.save(transaction);
        } catch (Exception e) {
            // Jika penyimpanan ke DB gagal, hapus file fisik yang sudah ter-upload
            deleteProofFile(uniqueFileName);
            throw new RuntimeException("Gagal menyimpan data transaksi ke database.", e);
        }
    }

    /**
     * Method private untuk menyimpan file fisik.
     * Termasuk validasi, pembuatan nama unik, dan penyimpanan ke disk.
     */
    private String storeProofFile(MultipartFile file) {
        // Validasi file (ukuran, ekstensi, dll)
        validateProofFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;

            Path targetLocation = this.proofStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file " + originalFilename, e);
        }
    }

    /**
     * Method private untuk validasi data dari DTO.
     */
    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getPaymentType() == null || request.getPaymentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Jenis pembayaran tidak boleh kosong");
        }
        if (request.getRemainingPayment() == null || request.getRemainingPayment().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah pembayaran tidak valid");
        }
        if (request.getRpProofUrl() == null || request.getRpProofUrl().isEmpty()) {
            throw new IllegalArgumentException("Bukti pembayaran harus di-upload");
        }
    }

    /**
     * Method private untuk validasi file yang di-upload.
     */
    private void validateProofFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Ukuran file melebihi batas maksimal 5MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new RuntimeException("Nama file tidak valid");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("Format file tidak diizinkan. Gunakan: JPG, JPEG, PNG");
        }
    }

    /**
     * Method private untuk menghapus file fisik dari storage.
     */
    private void deleteProofFile(String filename) {
        if (filename == null || filename.isBlank())
            return;
        try {
            Files.deleteIfExists(proofStorageLocation.resolve(filename));
        } catch (IOException e) {
            // Log error, tapi jangan hentikan proses.
            // Mungkin file tidak pernah dibuat atau ada masalah permission.
            System.err.println("Gagal menghapus file bukti: " + filename + " | Error: " + e.getMessage());
        }
    }

    /**
     * Method private helper untuk mendapatkan ekstensi file.
     */
    private String getFileExtension(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.'));
        } catch (Exception e) {
            throw new RuntimeException("Nama file tidak memiliki ekstensi yang valid.");
        }
    }

    // Anda bisa menambahkan method lain di sini sesuai kebutuhan,
    // misalnya untuk melihat riwayat transaksi, dll.
}