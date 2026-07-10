package com.sporty.jackpot.api;

import com.sporty.jackpot.service.ResourceNotFoundException;
import com.sporty.jackpot.service.RewardEvaluation;
import com.sporty.jackpot.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardService rewardService;

    @Test
    void winReturnsRewardAmount() throws Exception {
        when(rewardService.evaluate("bet-1"))
                .thenReturn(RewardEvaluation.win("bet-1", new BigDecimal("1500.0000")));

        mockMvc.perform(post("/api/bets/bet-1/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value("bet-1"))
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardAmount").value(1500.0000));
    }

    @Test
    void lossReturnsNoReward() throws Exception {
        when(rewardService.evaluate("bet-2")).thenReturn(RewardEvaluation.loss("bet-2"));

        mockMvc.perform(post("/api/bets/bet-2/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value("bet-2"))
                .andExpect(jsonPath("$.won").value(false))
                .andExpect(jsonPath("$.rewardAmount").doesNotExist());
    }

    @Test
    void unknownBetReturns404() throws Exception {
        when(rewardService.evaluate("ghost"))
                .thenThrow(new ResourceNotFoundException("No contribution found for betId 'ghost'"));

        mockMvc.perform(post("/api/bets/ghost/evaluate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
