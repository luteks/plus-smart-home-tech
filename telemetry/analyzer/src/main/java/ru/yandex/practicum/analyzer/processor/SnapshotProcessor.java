package ru.yandex.practicum.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.handler.snapshot.SensorsSnapshotHandler;
import ru.yandex.practicum.analyzer.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SnapshotProcessor {
    private final SensorsSnapshotHandler sensorsSnapshotHandler;
    private final Consumer<String, SensorsSnapshotAvro> snapshotConsumer;

    @Value("${collector.kafka.topics.snapshots-events}")
    private String snapshotEventsTopic;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    public SnapshotProcessor(SensorsSnapshotHandler sensorsSnapshotHandler, KafkaClient kafkaClient) {
        this.sensorsSnapshotHandler = sensorsSnapshotHandler;
        this.snapshotConsumer = kafkaClient.getKafkaSnapshotConsumer();
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(snapshotConsumer::wakeup));

        try {
            snapshotConsumer.subscribe(List.of(snapshotEventsTopic));

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                if (!records.isEmpty()) {
                    int count = 0;

                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        sensorsSnapshotHandler.handle(record.value());

                        saveOffsets(record, count, snapshotConsumer);
                        count++;
                    }

                    snapshotConsumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {
            log.info("Потребление сообщений из Kafka было прервано (штатное завершение работы)");
        } catch (Exception e) {
            log.error("Произошла ошибка обработки данных Снапшота}", e);
        } finally {
            try {
                snapshotConsumer.commitSync();
            } finally {
                snapshotConsumer.close();
            }
        }
    }

    private static void saveOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int count, Consumer<String, SensorsSnapshotAvro> consumer) {
        currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}