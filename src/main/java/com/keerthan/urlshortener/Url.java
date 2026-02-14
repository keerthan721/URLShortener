package com.keerthan.urlshortener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    private int clickCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters

    public Long getId() { return id; }

    public String getShortCode() { return shortCode; }

    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getOriginalUrl() { return originalUrl; }

    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public int getClickCount() { return clickCount; }

    public void setClickCount(int clickCount) { this.clickCount = clickCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
