package com.sporty.jackpot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A jackpot pool. Holds the pool balances plus the parameters that drive its
 * contribution and reward strategies. The concrete maths lives in the strategy
 * classes; this entity only stores the tunable inputs so a jackpot's behaviour
 * is fully data-driven.
 *
 * <p>Percentages/chances are stored as fractions (0.05 == 5%).</p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jackpot {

    @Id
    private String id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal initialPool;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPool;

    // ----- Contribution configuration -----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionType contributionType;

    /** FIXED: the flat contribution percentage. VARIABLE: the starting percentage. */
    @Column(precision = 10, scale = 6)
    private BigDecimal contributionPercentage;

    /** VARIABLE only: how fast the percentage decays per unit of pool growth above initialPool. */
    @Column(precision = 18, scale = 12)
    private BigDecimal contributionDecayRate;

    /** VARIABLE only: the floor the percentage never drops below. */
    @Column(precision = 10, scale = 6)
    private BigDecimal contributionFloorPercentage;

    // ----- Reward configuration -----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType rewardType;

    /** FIXED: the flat win chance. VARIABLE: the starting win chance. */
    @Column(precision = 10, scale = 6)
    private BigDecimal rewardChance;

    /** VARIABLE only: the pool value at (and above) which the win chance reaches 100%. */
    @Column(precision = 19, scale = 4)
    private BigDecimal rewardPoolLimit;

    @Version
    private Long version;
}
