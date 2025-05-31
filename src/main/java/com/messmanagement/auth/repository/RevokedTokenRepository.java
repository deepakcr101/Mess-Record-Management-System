package com.messmanagement.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.messmanagement.auth.entity.RevokedToken;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByJti(String jti);
    boolean existsByJti(String jti);
    void deleteByExpiryDateBefore(Instant now); // For cleanup
}