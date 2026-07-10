package com.sporty.jackpot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An immutable snapshot of a single bet's contribution to a jackpot pool.
 * Persisting this row is the durable trace that a given bet was processed, and
 * is what the reward evaluation later looks up by {@code betId}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JackpotContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String betId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String jackpotId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal stakeAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal contributionAmount;

    /** The pool value immediately after this contribution was applied. */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentJackpotAmount;

    @Column(nullable = false)
    private Instant createdAt;
}
