package ru.yandex.practicum.analyzer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "collector.kafka.consumer.hub.properties")
    public Properties getKafkaConsumerHubProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "collector.kafka.consumer.snapshot.properties")
    public Properties getKafkaConsumerSnapshotProperties() {
        return new Properties();
    }

    @Bean
    KafkaClient getKafkaClient() {
        return new KafkaClient() {
            private Consumer<String, HubEventAvro> kafkaHubConsumer;
            private Consumer<String, SensorsSnapshotAvro> kafkaSnapshotConsumer;

            @Override
            public Consumer<String, HubEventAvro> getKafkaHubConsumer() {
                kafkaHubConsumer = new KafkaConsumer<>(getKafkaConsumerHubProperties());

                return kafkaHubConsumer;
            }

            @Override
            public Consumer<String, SensorsSnapshotAvro> getKafkaSnapshotConsumer() {
                kafkaSnapshotConsumer = new KafkaConsumer<>(getKafkaConsumerSnapshotProperties());

                return kafkaSnapshotConsumer;
            }

            @Override
            public void close() {
                try {
                    kafkaHubConsumer.commitSync();
                    kafkaSnapshotConsumer.commitSync();
                } finally {
                    kafkaHubConsumer.close();
                    kafkaSnapshotConsumer.close();
                }
            }
        };
    }
}