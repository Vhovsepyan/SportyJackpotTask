package com.sporty.jackpot;

import com.sporty.jackpot.api.dto.BetAcceptedResponse;
import com.sporty.jackpot.api.dto.EvaluationResponse;
import com.sporty.jackpot.api.dto.PlaceBetRequest;
import com.sporty.jackpot.config.DataSeeder;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end flow on the default mock profile: place a bet over HTTP, verify the
 * contribution was recorded and the pool grew, then evaluate the bet and verify
 * a valid win/lose response. Exercises the full controller -> publisher ->
 * service -> repository path with no external dependencies.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
class JackpotFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JackpotRepository jackpotRepository;

    @Autowired
    private JackpotContributionRepository contributionRepository;

    @Test
    void placeBetThenEvaluate() {
        String betId = "it-bet-1";
        BigDecimal stake = new BigDecimal("200.00");

        BigDecimal poolBefore = jackpotRepository.findById(DataSeeder.FIXED_JACKPOT_ID)
                .orElseThrow().getCurrentPool();

        // 1. Place the bet -> 202 Accepted (processed inline in mock mode).
        PlaceBetRequest request = new PlaceBetRequest(betId, "user-1", DataSeeder.FIXED_JACKPOT_ID, stake);
        ResponseEntity<BetAcceptedResponse> placeResponse =
                restTemplate.postForEntity("/api/bets", request, BetAcceptedResponse.class);

        assertThat(placeResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(placeResponse.getBody()).isNotNull();
        assertThat(placeResponse.getBody().betId()).isEqualTo(betId);

        // 2. Contribution recorded with the correct snapshot (5% of 200 = 10).
        Optional<JackpotContribution> contribution = contributionRepository.findByBetId(betId);
        assertThat(contribution).isPresent();
        assertThat(contribution.get().getContributionAmount()).isEqualByComparingTo("10.0000");

        // 3. Pool increased by the contribution.
        Jackpot after = jackpotRepository.findById(DataSeeder.FIXED_JACKPOT_ID).orElseThrow();
        assertThat(after.getCurrentPool()).isEqualByComparingTo(poolBefore.add(new BigDecimal("10")));

        // 4. Evaluate -> 200 with a valid win/lose payload.
        ResponseEntity<EvaluationResponse> evalResponse = restTemplate.postForEntity(
                "/api/bets/{betId}/evaluate", null, EvaluationResponse.class, betId);

        assertThat(evalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(evalResponse.getBody()).isNotNull();
        assertThat(evalResponse.getBody().betId()).isEqualTo(betId);
        if (evalResponse.getBody().won()) {
            assertThat(evalResponse.getBody().rewardAmount()).isNotNull();
        } else {
            assertThat(evalResponse.getBody().rewardAmount()).isNull();
        }
    }
}
