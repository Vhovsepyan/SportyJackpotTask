package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.domain.JackpotReward;
import com.sporty.jackpot.domain.RewardType;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.FixedChanceReward;
import com.sporty.jackpot.strategy.reward.RewardStrategyFactory;
import com.sporty.jackpot.strategy.reward.VariableChanceReward;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @Mock
    private JackpotRewardRepository rewardRepository;

    @Captor
    private ArgumentCaptor<JackpotReward> rewardCaptor;

    private final RewardStrategyFactory rewardStrategyFactory = new RewardStrategyFactory(
            List.of(new FixedChanceReward(), new VariableChanceReward()));

    private RewardService service(RandomProvider randomProvider) {
        return new RewardService(jackpotRepository, contributionRepository,
                rewardRepository, rewardStrategyFactory, randomProvider);
    }

    /** FIXED reward with a flat 10% win chance and a pool of 1500. */
    private Jackpot jackpotWithPool(String currentPool) {
        return Jackpot.builder()
                .id("jackpot-fixed")
                .initialPool(new BigDecimal("1000.0000"))
                .currentPool(new BigDecimal(currentPool))
                .contributionType(ContributionType.FIXED)
                .contributionPercentage(new BigDecimal("0.050000"))
                .rewardType(RewardType.FIXED)
                .rewardChance(new BigDecimal("0.100000"))
                .build();
    }

    private JackpotContribution contribution() {
        return JackpotContribution.builder()
                .id(1L)
                .betId("bet-1")
                .userId("user-1")
                .jackpotId("jackpot-fixed")
                .stakeAmount(new BigDecimal("100.0000"))
                .contributionAmount(new BigDecimal("5.0000"))
                .currentJackpotAmount(new BigDecimal("1500.0000"))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void winPersistsRewardAndResetsPool() {
        Jackpot jackpot = jackpotWithPool("1500.0000");
        when(rewardRepository.findByBetId("bet-1")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("bet-1")).thenReturn(Optional.of(contribution()));
        when(jackpotRepository.findById("jackpot-fixed")).thenReturn(Optional.of(jackpot));

        // roll 0.05 < chance 0.10 -> win
        RewardEvaluation result = service(() -> 0.05).evaluate("bet-1");

        assertThat(result.won()).isTrue();
        assertThat(result.rewardAmount()).isEqualByComparingTo("1500.0000");

        verify(rewardRepository).save(rewardCaptor.capture());
        JackpotReward savedReward = rewardCaptor.getValue();
        assertThat(savedReward.getBetId()).isEqualTo("bet-1");
        assertThat(savedReward.getUserId()).isEqualTo("user-1");
        assertThat(savedReward.getJackpotRewardAmount()).isEqualByComparingTo("1500.0000");

        // Pool reset to initial and saved.
        assertThat(jackpot.getCurrentPool()).isEqualByComparingTo("1000.0000");
        verify(jackpotRepository).save(jackpot);
    }

    @Test
    void lossChangesNothing() {
        Jackpot jackpot = jackpotWithPool("1500.0000");
        when(rewardRepository.findByBetId("bet-1")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("bet-1")).thenReturn(Optional.of(contribution()));
        when(jackpotRepository.findById("jackpot-fixed")).thenReturn(Optional.of(jackpot));

        // roll 0.5 >= chance 0.10 -> loss
        RewardEvaluation result = service(() -> 0.5).evaluate("bet-1");

        assertThat(result.won()).isFalse();
        assertThat(result.rewardAmount()).isNull();
        assertThat(jackpot.getCurrentPool()).isEqualByComparingTo("1500.0000");
        verify(rewardRepository, never()).save(any());
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    void evaluationIsIdempotentWhenRewardAlreadyExists() {
        JackpotReward existing = JackpotReward.builder()
                .betId("bet-1")
                .userId("user-1")
                .jackpotId("jackpot-fixed")
                .jackpotRewardAmount(new BigDecimal("1500.0000"))
                .createdAt(Instant.now())
                .build();
        when(rewardRepository.findByBetId("bet-1")).thenReturn(Optional.of(existing));

        // A random provider that would always "win" — must not be consulted.
        RewardEvaluation result = service(() -> {
            throw new AssertionError("random roll must not be used when reward exists");
        }).evaluate("bet-1");

        assertThat(result.won()).isTrue();
        assertThat(result.rewardAmount()).isEqualByComparingTo("1500.0000");
        verify(rewardRepository, never()).save(any());
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    void unknownBetThrowsNotFound() {
        when(rewardRepository.findByBetId("ghost")).thenReturn(Optional.empty());
        when(contributionRepository.findByBetId("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service(() -> 0.0).evaluate("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("never contributed");
    }
}
