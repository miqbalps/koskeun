package com.example.koskeun.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Kolom ID dan Relasi (Tidak Berubah) ---
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "kos_id")
    private Long kosId;

    @Column(name = "owner_id")
    private Long ownerId;

    // --- PENAMBAHAN: Kolom untuk Periode Sewa ---
    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE) // Hanya menyimpan tanggal, tanpa waktu
    private Date startDate; // Tanggal Mulai Ngekos

    @Column(name = "end_date", nullable = false)
    @Temporal(TemporalType.DATE) // Hanya menyimpan tanggal, tanpa waktu
    private Date endDate; // Tanggal Selesai Ngekos

    @Column(name = "duration_in_months")
    private Integer durationInMonths; // Opsional: untuk menyimpan durasi sewa

    // --- PERUBAHAN NAMA & OTOMATISASI: Kolom Waktu ---
    @CreationTimestamp // Otomatis diisi saat record dibuat
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Menggantikan book_date, menandakan kapan booking diajukan

    @UpdateTimestamp // Otomatis diisi saat record di-update
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Tambahan: untuk melacak kapan terakhir diubah

    @Column(name = "payment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate; // Menggantikan transaction_date, tanggal bayar pertama

    @Column(name = "reviewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedAt;

    // --- Kolom Pembayaran & Status (Tidak Berubah) ---
    @Column(length = 20)
    private String paymentType;

    @Column(name = "down_payment", precision = 10, scale = 2)
    private BigDecimal downPayment;

    @Column(columnDefinition = "text")
    private String dpProofUrl;

    @Column(name = "remaining_payment", precision = 10, scale = 2)
    private BigDecimal remainingPayment;

    @Column(columnDefinition = "text")
    private String rpProofUrl;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(length = 20)
    private String status;

    // --- Relasi (Tidak Berubah) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kos_id", insertable = false, updatable = false)
    private Kos kos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    public Transaction() {
        this.status = "PENDING"; // Status awal saat booking dibuat
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getKosId() {
        return kosId;
    }

    public void setKosId(Long kosId) {
        this.kosId = kosId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public String getDpProofUrl() {
        return dpProofUrl;
    }

    public void setDpProofUrl(String dpProofUrl) {
        this.dpProofUrl = dpProofUrl;
    }

    public BigDecimal getRemainingPayment() {
        return remainingPayment;
    }

    public void setRemainingPayment(BigDecimal remainingPayment) {
        this.remainingPayment = remainingPayment;
    }

    public String getRpProofUrl() {
        return rpProofUrl;
    }

    public void setRpProofUrl(String rpProofUrl) {
        this.rpProofUrl = rpProofUrl;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Kos getKos() {
        return kos;
    }

    public void setKos(Kos kos) {
        this.kos = kos;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
