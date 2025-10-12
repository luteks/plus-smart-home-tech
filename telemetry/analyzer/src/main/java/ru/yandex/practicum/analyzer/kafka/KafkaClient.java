package ru.yandex.practicum.analyzer.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public interface KafkaClient extends AutoCloseable {
    Consumer<String, HubEventAvro> getKafkaHubConsumer();

    Consumer<String, SensorsSnapshotAvro> getKafkaSnapshotConsumer();
}