package ru.yandex.practicum.service.converter;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.kafka.CollectorClientProducer;

@Slf4j
public abstract class EventConverter<T, A extends SpecificRecordBase> {

    public abstract A convert(T event);

    public void sendEvent(CollectorClientProducer kafkaProducer, String topic, String key, T event) {
        log.info("Обрабатываю событие: {}", event);
        A avroEvent = convert(event);

        log.info("Отправляю Avro-событие в Kafka: {}", avroEvent);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, avroEvent);

        kafkaProducer.getProducer().send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке события в Kafka", exception);
            } else {
                log.debug("Событие успешно отправлено в топик");
            }
        });
    }
}