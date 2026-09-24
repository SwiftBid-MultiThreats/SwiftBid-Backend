package com.example.SwiftBid.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a product to be auctioned
 */
@Setter
@Getter
@Entity
@Table(name = "products")
public class Product {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    // Deliberately NOT @Lob: keeps it a plain STRING-typed column (still DDL'd as TEXT) so HQL
    // string functions like LOWER() used by ProductRepository.search() work (a @Lob/CLOB-mapped
    // field fails Hibernate 6's function-argument type validation for LOWER()).
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "initial_price", nullable = false)
    private BigDecimal initialPrice;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    // Constructors
    public Product() {
        this.createdAt = Instant.now();
    }

}
