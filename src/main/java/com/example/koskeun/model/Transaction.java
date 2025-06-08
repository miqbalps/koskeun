package com.example.koskeun.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "kos_id")
    private Long kosId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

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

    @Column(name = "reviewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "kos_id", insertable = false, updatable = false)
    private Kos kos;

    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    public Transaction() {
        this.transactionDate = new Date();
        this.status = "pending"; // Default status
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

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
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
