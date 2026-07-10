package com.sporty.jackpot.config;

import com.sporty.jackpot.kafka.KafkaBetPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the bets topic so it is created automatically when the app starts
 * against a real broker (kafka profile only).
 */
@Configuration
@Profile("kafka")
public class KafkaTopicConfig {

    @Bean
    public NewTopic jackpotBetsTopic() {
        return TopicBuilder.name(KafkaBetPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
