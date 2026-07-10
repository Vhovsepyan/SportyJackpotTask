package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JackpotRepositoryTest {

    @Autowired
    private JackpotRepository jackpotRepository;

    @Test
    void savesAndFindsJackpotById() {
        Jackpot jackpot = Jackpot.builder()
                .id("jackpot-test")
                .initialPool(new BigDecimal("1000.0000"))
                .currentPool(new BigDecimal("1250.0000"))
                .contributionType(ContributionType.FIXED)
                .contributionPercentage(new BigDecimal("0.050000"))
                .rewardType(RewardType.FIXED)
                .rewardChance(new BigDecimal("0.100000"))
                .build();

        jackpotRepository.save(jackpot);

        Optional<Jackpot> found = jackpotRepository.findById("jackpot-test");
        assertThat(found).isPresent();
        assertThat(found.get().getCurrentPool()).isEqualByComparingTo("1250.0000");
        assertThat(found.get().getContributionType()).isEqualTo(ContributionType.FIXED);
        assertThat(found.get().getRewardType()).isEqualTo(RewardType.FIXED);
        // @Version is populated once persisted.
        assertThat(found.get().getVersion()).isNotNull();
    }
}
