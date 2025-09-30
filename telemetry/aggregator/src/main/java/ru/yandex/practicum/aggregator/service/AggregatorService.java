package ru.yandex.practicum.aggregator.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface AggregatorService {
    void makeAggregationSnapshot(Producer<String, SpecificRecordBase> producer, SpecificRecordBase sensorEventAvro);
}
