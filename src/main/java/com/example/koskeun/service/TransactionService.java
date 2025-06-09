package com.example.koskeun.service;

import com.example.koskeun.dto.request.BookingRequest;
import com.example.koskeun.dto.request.PaymentRequest;
import com.example.koskeun.model.Kos;
import com.example.koskeun.model.Transaction;
import com.example.koskeun.model.User;
import com.example.koskeun.repository.KosRepository;
import com.example.koskeun.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private KosRepository kosRepository;
    @Autowired
    private AuthService authService;

    private final Path proofStorageLocation = Paths.get("uploads/payment-proofs");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");

    public TransactionService() {
        try {
            Files.createDirectories(proofStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload bukti pembayaran", e);
        }
    }

    /**
     * LANGKAH 1: PENYEWA MEMBUAT PERMINTAAN BOOKING
     */
    @Transactional
    public Transaction createBooking(Long kosId, BookingRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        if (currentUser == null)
            throw new SecurityException("User harus login untuk membuat booking.");

        Kos kos = kosRepository.findById(kosId)
                .orElseThrow(() -> new EntityNotFoundException("Kos dengan ID " + kosId + " tidak ditemukan."));

        // Kalkulasi harga total dan tanggal berakhir sewa
        BigDecimal total = kos.getPriceMonthly().multiply(new BigDecimal(request.getDurationInMonths()));
        Calendar cal = Calendar.getInstance();
        cal.setTime(request.getStartDate());
        cal.add(Calendar.MONTH, request.getDurationInMonths());
        Date endDate = cal.getTime();

        Transaction transaction = new Transaction();
        transaction.setKosId(kos.getId());
        transaction.setUserId(currentUser.getId());
        transaction.setOwnerId(kos.getOwnerId());
        transaction.setStartDate(request.getStartDate());
        transaction.setEndDate(endDate);
        transaction.setDurationInMonths(request.getDurationInMonths());
        transaction.setTotal(total);
        transaction.setStatus("PENDING"); // Status awal sudah diatur di constructor, ini hanya penegasan

        return transactionRepository.save(transaction);
    }

    /**
     * LANGKAH 2: PEMILIK MENYETUJUI ATAU MENOLAK BOOKING
     */
    @Transactional
    public Transaction reviewBooking(Long transactionId, boolean isApproved) {
        User currentUser = authService.getCurrentUserEntity();
        if (currentUser == null)
            throw new SecurityException("Akses ditolak.");

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaksi dengan ID " + transactionId + " tidak ditemukan."));

        if (!transaction.getOwnerId().equals(currentUser.getId())) {
            throw new SecurityException("Anda tidak berhak meninjau booking ini.");
        }

        if (!"PENDING".equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalStateException("Hanya booking dengan status PENDING yang bisa ditinjau.");
        }

        transaction.setStatus(isApproved ? "WAITING_PAYMENT" : "REJECTED");
        return transactionRepository.save(transaction);
    }

    /**
     * LANGKAH 3 & 5: PENYEWA MELAKUKAN PEMBAYARAN
     */
    @Transactional
    public Transaction processPayment(Long transactionId, PaymentRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        if (currentUser == null)
            throw new SecurityException("User harus login untuk membayar.");

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaksi dengan ID " + transactionId + " tidak ditemukan."));

        if (!transaction.getUserId().equals(currentUser.getId())) {
            throw new SecurityException("Anda tidak berhak melakukan pembayaran untuk booking ini.");
        }

        if (!List.of("WAITING_PAYMENT", "DOWN_PAYMENT").contains(transaction.getStatus().toUpperCase())) {
            throw new IllegalStateException("Pembayaran tidak dapat dilakukan untuk transaksi dengan status saat ini.");
        }

        String proofFileName = storeProofFile(request.getPaymentProof());

        try {
            transaction.setPaymentType(request.getPaymentType());
            transaction.setPaymentDate(new Date());

            // LOGIKA BARU BERDASARKAN TIPE PEMBAYARAN
            if ("down_payment".equalsIgnoreCase(request.getPaymentType())) {
                // ---- SKENARIO BAYAR DP ----
                if (request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Jumlah DP harus diisi dan lebih dari nol.");
                }

                // Cek apakah ini pembayaran DP pertama atau pelunasan
                if (transaction.getDownPayment() == null) { // Ini DP pertama
                    transaction.setDownPayment(request.getPaymentAmount());
                    transaction.setDpProofUrl(proofFileName);
                    // Hitung sisa pembayaran
                    BigDecimal remaining = transaction.getTotal().subtract(request.getPaymentAmount());
                    transaction.setRemainingPayment(remaining);
                } else { // Ini pelunasan
                    transaction.setRemainingPayment(request.getPaymentAmount());
                    transaction.setRpProofUrl(proofFileName);
                }

            } else if ("full_payment".equalsIgnoreCase(request.getPaymentType())) {
                // ---- SKENARIO BAYAR LUNAS ----
                // Saat bayar lunas, down_payment diisi sejumlah total, sisanya 0.
                transaction.setDownPayment(transaction.getTotal());
                transaction.setRemainingPayment(BigDecimal.ZERO);
                transaction.setDpProofUrl(proofFileName); // Bukti disimpan sebagai bukti DP/Lunas
            } else {
                throw new IllegalArgumentException("Tipe pembayaran tidak valid.");
            }

            transaction.setStatus("WAITING_CONFIRMATION");
            return transactionRepository.save(transaction);

        } catch (Exception e) {
            deleteProofFile(proofFileName); // Rollback file jika save DB gagal
            throw e; // Lemparkan kembali error asli setelah cleanup
        }
    }

    /**
     * LANGKAH 4: PEMILIK MENGKONFIRMASI PEMBAYARAN
     */
    @Transactional
    public Transaction confirmPayment(Long transactionId, boolean isConfirmed) {
        User currentUser = authService.getCurrentUserEntity();
        if (currentUser == null)
            throw new SecurityException("Akses ditolak.");

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaksi dengan ID " + transactionId + " tidak ditemukan."));

        if (!transaction.getOwnerId().equals(currentUser.getId())) {
            throw new SecurityException("Anda tidak berhak mengkonfirmasi pembayaran ini.");
        }

        if (!"WAITING_CONFIRMATION".equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalStateException(
                    "Hanya pembayaran dengan status WAITING_CONFIRMATION yang bisa dikonfirmasi.");
        }

        if (isConfirmed) {
            // Cek apakah pembayaran sudah lunas
            BigDecimal totalPaid = transaction.getDownPayment();
            if (transaction.getRemainingPayment() != null) {
                totalPaid = totalPaid.add(transaction.getRemainingPayment());
            }

            if (totalPaid.compareTo(transaction.getTotal()) >= 0) {
                transaction.setStatus("COMPLETED");
            } else {
                transaction.setStatus("DOWN_PAYMENT");
            }
        } else {
            transaction.setStatus("REJECTED"); // Pembayaran ditolak
        }
        return transactionRepository.save(transaction);
    }

    // Method untuk mengambil daftar transaksi
    public List<Transaction> getTransactionsForCurrentUser() {
        User currentUser = authService.getCurrentUserEntity();
        if (currentUser == null)
            throw new RuntimeException("User tidak terautentikasi.");

        String role = currentUser.getRole().getName(); // Asumsi ada getRole().getName()
        Long currentUserId = currentUser.getId();

        return switch (role.toUpperCase()) {
            case "ADMIN" -> transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "PEMILIK" ->
                transactionRepository.findByOwnerId(currentUserId, Sort.by(Sort.Direction.DESC, "createdAt"));
            case "PENCARI" -> transactionRepository.findByUserId(currentUserId);
            default -> Collections.emptyList();
        };
    }

    /**
     * KODE BARU YANG PERLU DITAMBAHKAN
     * =================================================================
     * Mengambil satu data transaksi berdasarkan ID, dengan validasi keamanan.
     * Method ini memastikan hanya pengguna yang bersangkutan (penyewa atau pemilik)
     * atau admin yang bisa mengakses data transaksi ini.
     *
     * @param transactionId ID dari transaksi yang dicari.
     * @param currentUser   Objek User yang sedang login.
     * @return Entitas Transaction jika ditemukan dan diotorisasi.
     */
    public Transaction getTransactionByIdForUser(Long transactionId, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Akses ditolak. Anda harus login.");
        }

        // 1. Ambil transaksi dari database
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaksi dengan ID " + transactionId + " tidak ditemukan."));

        // 2. Cek hak akses
        String userRole = currentUser.getRole().getName();

        // Admin bisa melihat semua transaksi
        if (userRole.equalsIgnoreCase("ADMIN")) {
            return transaction;
        }

        // Cek apakah user yang login adalah pemilik ATAU penyewa dari transaksi ini
        boolean isOwner = transaction.getOwnerId().equals(currentUser.getId());
        boolean isRenter = transaction.getUserId().equals(currentUser.getId());

        if (isOwner || isRenter) {
            return transaction;
        } else {
            // Jika bukan keduanya, tolak akses
            throw new SecurityException("Anda tidak memiliki izin untuk mengakses transaksi ini.");
        }
    }

    // Method-method helper untuk file
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getPaymentType() == null || request.getPaymentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Jenis pembayaran tidak boleh kosong");
        }
        if (request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah pembayaran tidak valid");
        }
        if (request.getPaymentProof() == null || request.getPaymentProof().isEmpty()) {
            throw new IllegalArgumentException("Bukti pembayaran harus di-upload");
        }
    }

    private String storeProofFile(MultipartFile file) {
        validateProofFile(file);
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
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

    private void validateProofFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE)
            throw new RuntimeException("Ukuran file melebihi batas 5MB");
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty())
            throw new RuntimeException("Nama file tidak valid");
        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase()))
            throw new RuntimeException("Format file tidak diizinkan: JPG, JPEG, PNG");
    }

    private void deleteProofFile(String filename) {
        if (filename == null || filename.isBlank())
            return;
        try {
            Files.deleteIfExists(proofStorageLocation.resolve(filename));
        } catch (IOException e) {
            System.err.println("Gagal menghapus file bukti: " + filename + " | Error: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.'));
        } catch (Exception e) {
            throw new RuntimeException("Nama file tidak memiliki ekstensi yang valid.");
        }
    }
}