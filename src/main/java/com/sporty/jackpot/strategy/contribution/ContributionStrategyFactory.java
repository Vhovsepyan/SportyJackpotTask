package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the {@link ContributionStrategy} for a given {@link ContributionType}.
 *
 * <p>Strategies are discovered from the Spring context and indexed by the type
 * each one declares, so registering a new strategy requires no change here.</p>
 */
@Component
public class ContributionStrategyFactory {

    private final Map<ContributionType, ContributionStrategy> strategiesByType =
            new EnumMap<>(ContributionType.class);

    public ContributionStrategyFactory(List<ContributionStrategy> strategies) {
        for (ContributionStrategy strategy : strategies) {
            ContributionStrategy existing = strategiesByType.put(strategy.supportedType(), strategy);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate contribution strategy for type " + strategy.supportedType());
            }
        }
    }

    public ContributionStrategy strategyFor(ContributionType type) {
        ContributionStrategy strategy = strategiesByType.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No contribution strategy registered for type: " + type);
        }
        return strategy;
    }
}
