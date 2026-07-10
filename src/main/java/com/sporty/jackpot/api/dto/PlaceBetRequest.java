package com.sporty.jackpot.api.dto;

import com.sporty.jackpot.domain.Bet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for placing a bet. Validated on the way in; all identifiers are
 * required and the stake must be strictly positive.
 */
public record PlaceBetRequest(

        @NotBlank(message = "betId is required")
        String betId,

        @NotBlank(message = "userId is required")
        String userId,

        @NotBlank(message = "jackpotId is required")
        String jackpotId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than 0")
        BigDecimal amount
) {
    public Bet toBet() {
        return new Bet(betId, userId, jackpotId, amount);
    }
}
