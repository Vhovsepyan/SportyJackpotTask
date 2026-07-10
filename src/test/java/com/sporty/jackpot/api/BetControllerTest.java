package com.sporty.jackpot.api;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.kafka.BetPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BetController.class)
class BetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BetPublisher betPublisher;

    @Test
    void validBetReturns202AndPublishes() throws Exception {
        String body = """
                {"betId":"bet-1","userId":"user-1","jackpotId":"jackpot-fixed","amount":100.00}
                """;

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.betId").value("bet-1"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        ArgumentCaptor<Bet> captor = ArgumentCaptor.forClass(Bet.class);
        verify(betPublisher).publish(captor.capture());
        assertThat(captor.getValue().jackpotId()).isEqualTo("jackpot-fixed");
        assertThat(captor.getValue().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void invalidBetReturns400WithFieldErrors() throws Exception {
        // blank betId, negative amount
        String body = """
                {"betId":"","userId":"user-1","jackpotId":"jackpot-fixed","amount":-5}
                """;

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.betId").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());

        verify(betPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void duplicateBetIdConflictReturns409() throws Exception {
        // A racing duplicate that trips the betId unique constraint surfaces as 409, not 500.
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("unique betId"))
                .when(betPublisher).publish(org.mockito.ArgumentMatchers.any());

        String body = """
                {"betId":"bet-1","userId":"user-1","jackpotId":"jackpot-fixed","amount":100.00}
                """;

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
