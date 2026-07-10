package com.sporty.jackpot.kafka;

import com.sporty.jackpot.domain.Bet;

/**
 * Publishes bets for jackpot processing. Two implementations are selected by
 * profile: {@link MockBetPublisher} (default, in-process) and
 * {@link KafkaBetPublisher} (real broker). The rest of the application depends
 * only on this interface.
 */
public interface BetPublisher {

    void publish(Bet bet);
}
