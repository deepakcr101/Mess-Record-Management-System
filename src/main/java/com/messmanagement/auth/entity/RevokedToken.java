package com.messmanagement.auth.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; // Use Instant for timestamps
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "revoked_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti; // The JWT ID of the revoked token

    @Column(nullable = false)
    private Instant expiryDate; // When this revocation record itself can be cleaned up (matches original token expiry)

    public RevokedToken(String jti, Instant expiryDate) {
        this.jti = jti;
        this.expiryDate = expiryDate;
    }
}