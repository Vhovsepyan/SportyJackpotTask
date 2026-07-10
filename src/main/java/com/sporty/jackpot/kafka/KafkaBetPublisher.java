package com.sporty.jackpot.kafka;

import com.sporty.jackpot.domain.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka profile publisher. Serialises the bet as JSON and sends it to the
 * {@code jackpot-bets} topic, keyed by betId so a bet's messages keep ordering.
 */
@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaBetPublisher implements BetPublisher {

    public static final String TOPIC = "jackpot-bets";

    private final KafkaTemplate<String, Bet> kafkaTemplate;

    @Override
    public void publish(Bet bet) {
        log.info("[KAFKA] Publishing bet {} to topic '{}'", bet, TOPIC);
        kafkaTemplate.send(TOPIC, bet.betId(), bet);
    }
}
