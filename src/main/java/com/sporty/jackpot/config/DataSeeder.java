package com.sporty.jackpot.config;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;
import com.sporty.jackpot.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds two demonstration jackpots on startup so the service is usable out of
 * the box: one fully FIXED and one fully VARIABLE. IDs are logged clearly so
 * they can be copied into curl calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    public static final String FIXED_JACKPOT_ID = "jackpot-fixed";
    public static final String VARIABLE_JACKPOT_ID = "jackpot-variable";

    private final JackpotRepository jackpotRepository;

    @Override
    public void run(String... args) {
        seedFixedJackpot();
        seedVariableJackpot();
        log.info("Seeded jackpots: [{}] (FIXED/FIXED), [{}] (VARIABLE/VARIABLE)",
                FIXED_JACKPOT_ID, VARIABLE_JACKPOT_ID);
    }

    private void seedFixedJackpot() {
        if (jackpotRepository.existsById(FIXED_JACKPOT_ID)) {
            return;
        }
        Jackpot fixed = Jackpot.builder()
                .id(FIXED_JACKPOT_ID)
                .initialPool(new BigDecimal("1000.0000"))
                .currentPool(new BigDecimal("1000.0000"))
                .contributionType(ContributionType.FIXED)
                .contributionPercentage(new BigDecimal("0.050000")) // 5% of every stake
                .rewardType(RewardType.FIXED)
                .rewardChance(new BigDecimal("0.100000")) // flat 10% win chance
                .build();
        jackpotRepository.save(fixed);
    }

    private void seedVariableJackpot() {
        if (jackpotRepository.existsById(VARIABLE_JACKPOT_ID)) {
            return;
        }
        Jackpot variable = Jackpot.builder()
                .id(VARIABLE_JACKPOT_ID)
                .initialPool(new BigDecimal("1000.0000"))
                .currentPool(new BigDecimal("1000.0000"))
                .contributionType(ContributionType.VARIABLE)
                .contributionPercentage(new BigDecimal("0.100000"))        // starts at 10%
                .contributionDecayRate(new BigDecimal("0.000010000000"))   // -1% per 1000 of pool growth
                .contributionFloorPercentage(new BigDecimal("0.010000"))   // never below 1%
                .rewardType(RewardType.VARIABLE)
                .rewardChance(new BigDecimal("0.010000"))                  // starts at 1%
                .rewardPoolLimit(new BigDecimal("10000.0000"))             // 100% once pool reaches 10k
                .build();
        jackpotRepository.save(variable);
    }
}
