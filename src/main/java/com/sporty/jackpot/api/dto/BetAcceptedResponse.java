package com.sporty.jackpot.api.dto;

/**
 * Acknowledgement returned when a bet has been accepted for asynchronous
 * processing (HTTP 202).
 */
public record BetAcceptedResponse(String betId, String status) {

    public static BetAcceptedResponse accepted(String betId) {
        return new BetAcceptedResponse(betId, "ACCEPTED");
    }
}
