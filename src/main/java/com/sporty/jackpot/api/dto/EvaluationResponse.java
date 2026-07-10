package com.sporty.jackpot.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sporty.jackpot.service.RewardEvaluation;

import java.math.BigDecimal;

/**
 * Response body for a reward evaluation. {@code rewardAmount} is omitted when the
 * bet did not win.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EvaluationResponse(String betId, boolean won, BigDecimal rewardAmount) {

    public static EvaluationResponse from(RewardEvaluation evaluation) {
        return new EvaluationResponse(evaluation.betId(), evaluation.won(), evaluation.rewardAmount());
    }
}
