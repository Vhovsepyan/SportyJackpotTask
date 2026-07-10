package com.sporty.jackpot.api;

import com.sporty.jackpot.api.dto.BetAcceptedResponse;
import com.sporty.jackpot.api.dto.PlaceBetRequest;
import com.sporty.jackpot.kafka.BetPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for placing bets. Publishing is asynchronous, so a valid
 * request is acknowledged with 202 Accepted and processed off the request thread
 * (inline in mock mode, via Kafka in kafka mode).
 */
@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class BetController {

    private final BetPublisher betPublisher;

    @PostMapping
    public ResponseEntity<BetAcceptedResponse> placeBet(@Valid @RequestBody PlaceBetRequest request) {
        betPublisher.publish(request.toBet());
        return ResponseEntity.accepted().body(BetAcceptedResponse.accepted(request.betId()));
    }
}
