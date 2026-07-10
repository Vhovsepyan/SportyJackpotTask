package com.sporty.jackpot.kafka;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.service.ContributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka profile consumer. Listens on the {@code jackpot-bets} topic and hands
 * each bet to the same {@link ContributionService} the mock publisher uses.
 */
@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class BetConsumer {

    private final ContributionService contributionService;

    @KafkaListener(topics = KafkaBetPublisher.TOPIC, groupId = "jackpot-service")
    public void onBet(Bet bet) {
        log.info("[KAFKA] Received bet {}", bet);
        contributionService.processBet(bet);
    }
}
