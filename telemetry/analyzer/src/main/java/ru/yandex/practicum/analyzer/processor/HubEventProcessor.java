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
import ru.yandex.practicum.analyzer.handler.hub.HubEventHandler;
import ru.yandex.practicum.analyzer.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final Consumer<String, HubEventAvro> hubConsumer;
    private final Map<String, HubEventHandler> hubEventHandlers;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);


    @Value("${collector.kafka.topics.hubs-events}")
    private String hubEventsTopic;

    public HubEventProcessor(KafkaClient kafkaClient, List<HubEventHandler> hubEventHandlers) {
        this.hubConsumer = kafkaClient.getKafkaHubConsumer();
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getHubEventType, Function.identity()));
    }

    @Override
    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(hubConsumer::wakeup));

            hubConsumer.subscribe(List.of(hubEventsTopic));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = hubConsumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                if (!records.isEmpty()) {
                    int count = 0;

                    for (ConsumerRecord<String, HubEventAvro> record : records) {
                        HubEventAvro event = record.value();

                        String eventPayloadName = event.getPayload().getClass().getSimpleName();

                        HubEventHandler eventHandler;

                        if (hubEventHandlers.containsKey(eventPayloadName)) {
                            eventHandler = hubEventHandlers.get(eventPayloadName);
                        } else {
                            throw new IllegalArgumentException("Подходящий handler не найден");
                        }

                        eventHandler.handle(event);

                        saveOffsets(record, count, hubConsumer);
                        count++;
                    }

                    hubConsumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Произошла ошибка обработки данных от Хаба}", e);
        } finally {
            try {
                hubConsumer.commitSync();
            } finally {
                hubConsumer.close();
            }
        }
    }

    private static void saveOffsets(ConsumerRecord<String, HubEventAvro> record, int count, Consumer<String, HubEventAvro> consumer) {
        currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка закрепления оффсета: {}", offsets, exception);
                }
            });
        }
    }
}