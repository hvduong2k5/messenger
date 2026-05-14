package com.team12345.messenger.repository;

import com.team12345.messenger.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
