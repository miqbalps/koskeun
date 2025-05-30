package com.example.koskeun.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "kos_images")
public class KosImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kos_id")
    private Long kosId;

    @Column(length = 30)
    private String type;

    @Column(columnDefinition = "text")
    private String url;

    @ManyToOne
    @JoinColumn(name = "kos_id", insertable = false, updatable = false)
    private Kos kos;

    public KosImage() {
        // Default constructor
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKosId() {
        return kosId;
    }

    public void setKosId(Long kosId) {
        this.kosId = kosId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Kos getKos() {
        return kos;
    }

    public void setKos(Kos kos) {
        this.kos = kos;
    }
}
