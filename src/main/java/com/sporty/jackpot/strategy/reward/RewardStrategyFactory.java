package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.RewardType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the {@link RewardStrategy} for a given {@link RewardType}.
 *
 * <p>Strategies are discovered from the Spring context and indexed by the type
 * each one declares, so registering a new strategy requires no change here.</p>
 */
@Component
public class RewardStrategyFactory {

    private final Map<RewardType, RewardStrategy> strategiesByType =
            new EnumMap<>(RewardType.class);

    public RewardStrategyFactory(List<RewardStrategy> strategies) {
        for (RewardStrategy strategy : strategies) {
            RewardStrategy existing = strategiesByType.put(strategy.supportedType(), strategy);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate reward strategy for type " + strategy.supportedType());
            }
        }
    }

    public RewardStrategy strategyFor(RewardType type) {
        RewardStrategy strategy = strategiesByType.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No reward strategy registered for type: " + type);
        }
        return strategy;
    }
}
