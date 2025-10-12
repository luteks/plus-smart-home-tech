package ru.yandex.practicum.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.kafka.KafkaClient;
import ru.yandex.practicum.aggregator.service.AggregatorService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class AggregationStarter {
    private final AggregatorService aggregatorService;
    private final Producer<String, SpecificRecordBase> producer;
    private final Consumer<String, SpecificRecordBase> consumer;
    @Value("${collector.kafka.topics.sensors-events}")
    private String sensorsEventsTopic;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    public AggregationStarter(KafkaClient kafkaClient, AggregatorService aggregatorService) {
        this.producer = kafkaClient.getProducer();
        this.consumer = kafkaClient.getConsumer();
        this.aggregatorService = aggregatorService;

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            consumer.subscribe(List.of(sensorsEventsTopic));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    aggregatorService.makeAggregationSnapshot(producer, record.value());
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Потребление сообщений из Kafka было прервано (штатное завершение работы)");
        } catch (Exception e) {
            log.error("Произошла ошибка обработки данных от датчика в агрегаторе", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                consumer.close();
                producer.close(Duration.ofSeconds(5));
            }
        }
    }
}