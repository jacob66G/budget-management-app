package com.example.budget_management_app.transaction_receipts.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transaction_photos")
@Getter@Setter
@NoArgsConstructor
public class TransactionPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "key")
    private String key;

    public TransactionPhoto(String originalFileName, String key) {
        this.originalFileName = originalFileName;
        this.key = key;
    }
}
