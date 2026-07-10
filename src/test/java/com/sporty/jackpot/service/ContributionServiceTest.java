package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.domain.RewardType;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.strategy.contribution.ContributionStrategyFactory;
import com.sporty.jackpot.strategy.contribution.FixedPercentageContribution;
import com.sporty.jackpot.strategy.contribution.VariablePercentageContribution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @Captor
    private ArgumentCaptor<JackpotContribution> contributionCaptor;

    private final ContributionStrategyFactory strategyFactory = new ContributionStrategyFactory(
            List.of(new FixedPercentageContribution(), new VariablePercentageContribution()));

    private ContributionService service() {
        return new ContributionService(jackpotRepository, contributionRepository, strategyFactory);
    }

    private Jackpot fixedJackpot() {
        return Jackpot.builder()
                .id("jackpot-fixed")
                .initialPool(new BigDecimal("1000.0000"))
                .currentPool(new BigDecimal("1000.0000"))
                .contributionType(ContributionType.FIXED)
                .contributionPercentage(new BigDecimal("0.050000"))
                .rewardType(RewardType.FIXED)
                .rewardChance(new BigDecimal("0.100000"))
                .build();
    }

    @Test
    void incrementsPoolAndPersistsContributionSnapshot() {
        Jackpot jackpot = fixedJackpot();
        when(jackpotRepository.findById("jackpot-fixed")).thenReturn(Optional.of(jackpot));

        Bet bet = new Bet("bet-1", "user-1", "jackpot-fixed", new BigDecimal("100.0000"));
        service().processBet(bet);

        // Pool grew by 5% of 100 = 5.
        assertThat(jackpot.getCurrentPool()).isEqualByComparingTo("1005.0000");
        verify(jackpotRepository).save(jackpot);

        verify(contributionRepository).save(contributionCaptor.capture());
        JackpotContribution saved = contributionCaptor.getValue();
        assertThat(saved.getBetId()).isEqualTo("bet-1");
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getJackpotId()).isEqualTo("jackpot-fixed");
        assertThat(saved.getStakeAmount()).isEqualByComparingTo("100.0000");
        assertThat(saved.getContributionAmount()).isEqualByComparingTo("5.0000");
        assertThat(saved.getCurrentJackpotAmount()).isEqualByComparingTo("1005.0000");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void skipsUnknownJackpotWithoutError() {
        when(jackpotRepository.findById("nope")).thenReturn(Optional.empty());

        Bet bet = new Bet("bet-2", "user-2", "nope", new BigDecimal("100.0000"));
        service().processBet(bet);

        verify(jackpotRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(contributionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
