package com.sporty.jackpot.kafka;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.service.ContributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default (mock profile) publisher. Instead of going through a broker it logs
 * the payload and invokes the exact same processing code the Kafka consumer
 * would, so the app is fully usable with zero external dependencies.
 */
@Slf4j
@Component
@Profile("mock")
@RequiredArgsConstructor
public class MockBetPublisher implements BetPublisher {

    private final ContributionService contributionService;

    @Override
    public void publish(Bet bet) {
        log.info("[MOCK] Publishing bet {} — processing inline", bet);
        contributionService.processBet(bet);
    }
}
