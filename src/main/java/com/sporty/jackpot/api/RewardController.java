package com.sporty.jackpot.api;

import com.sporty.jackpot.api.dto.EvaluationResponse;
import com.sporty.jackpot.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for evaluating a bet against its jackpot. Evaluation is
 * synchronous and idempotent.
 */
@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @PostMapping("/{betId}/evaluate")
    public EvaluationResponse evaluate(@PathVariable String betId) {
        return EvaluationResponse.from(rewardService.evaluate(betId));
    }
}
