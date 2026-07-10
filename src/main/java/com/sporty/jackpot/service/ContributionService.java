package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.strategy.contribution.ContributionStrategy;
import com.sporty.jackpot.strategy.contribution.ContributionStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

/**
 * Applies a bet's contribution to its jackpot pool and records a durable
 * snapshot of that contribution. This is the single code path used by both the
 * mock publisher (inline) and the Kafka consumer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionService {

    /** Money is kept to 4 decimal places throughout. */
    static final int MONEY_SCALE = 4;

    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;
    private final ContributionStrategyFactory contributionStrategyFactory;

    /**
     * Processes a single bet: computes its contribution, grows the pool and
     * persists the contribution snapshot. If the referenced jackpot does not
     * exist the bet is logged and skipped rather than failing.
     *
     * <p>Runs in a transaction; the {@code @Version} column on {@link Jackpot}
     * provides optimistic locking so concurrent bets on the same pool are
     * serialised safely.</p>
     */
    @Transactional
    public void processBet(Bet bet) {
        // A betId identifies a single bet. Processing is at-least-once (HTTP retry
        // or Kafka redelivery), so contributing is idempotent: a betId already
        // seen is skipped rather than contributing to the pool twice.
        if (contributionRepository.existsByBetId(bet.betId())) {
            // Expected under at-least-once delivery / client retries — a benign no-op, not a warning.
            log.info("Contribution for betId '{}' already recorded; skipping duplicate bet", bet.betId());
            return;
        }

        Optional<Jackpot> maybeJackpot = jackpotRepository.findById(bet.jackpotId());
        if (maybeJackpot.isEmpty()) {
            log.warn("No jackpot found for id '{}' (betId '{}'); skipping contribution",
                    bet.jackpotId(), bet.betId());
            return;
        }

        Jackpot jackpot = maybeJackpot.get();
        ContributionStrategy strategy =
                contributionStrategyFactory.strategyFor(jackpot.getContributionType());

        BigDecimal contribution = strategy.calculate(bet.amount(), jackpot)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal updatedPool = jackpot.getCurrentPool().add(contribution)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        jackpot.setCurrentPool(updatedPool);
        jackpotRepository.save(jackpot);

        JackpotContribution snapshot = JackpotContribution.builder()
                .betId(bet.betId())
                .userId(bet.userId())
                .jackpotId(bet.jackpotId())
                .stakeAmount(bet.amount())
                .contributionAmount(contribution)
                .currentJackpotAmount(updatedPool)
                .createdAt(Instant.now())
                .build();
        contributionRepository.save(snapshot);

        log.info("Bet '{}' contributed {} to jackpot '{}' (pool now {})",
                bet.betId(), contribution, jackpot.getId(), updatedPool);
    }
}
