package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.CollectorClientProducer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.hub.HubEventMapper;
import ru.yandex.practicum.mapper.sensor.SensorEventMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CollectorService {
    @Value("${sensorEventTopic}")
    private String sensorsEventsTopic;
    @Value("${hubEventTopic}")
    private String hubsEventsTopic;

    private final CollectorClientProducer collectorClientProducer;
    private final Map<SensorEventProto.PayloadCase, SensorEventMapper> sensorEventMappers;
    private final Map<HubEventProto.PayloadCase, HubEventMapper> hubEventMappers;

    public CollectorService(
            CollectorClientProducer collectorClientProducer,
            List<SensorEventMapper> sensorEventMapperList,
            List<HubEventMapper> hubEventMapperList
    ) {
        this.collectorClientProducer = collectorClientProducer;
        this.sensorEventMappers = sensorEventMapperList.stream()
                .collect(Collectors.toMap(SensorEventMapper::getSensorEventType, Function.identity()));
        this.hubEventMappers = hubEventMapperList.stream()
                .collect(Collectors.toMap(HubEventMapper::getHubEventType, Function.identity()));
    }

    public void collectSensorEvent(SensorEventProto event) {
        SensorEventMapper eventMapper;
        if (sensorEventMappers.containsKey(event.getPayloadCase())) {
            eventMapper = sensorEventMappers.get(event.getPayloadCase());
        } else {
            throw new IllegalArgumentException("Нету подходящего маппера");
        }

        SensorEventAvro eventAvro = eventMapper.mapToAvro(event);

        collectorClientProducer.getProducer().send(new ProducerRecord<>(sensorsEventsTopic, eventAvro.getHubId(), eventAvro));
    }

    public void collectHubEvent(HubEventProto event) {
        HubEventMapper eventMapper;
        if (hubEventMappers.containsKey(event.getPayloadCase())) {
            eventMapper = hubEventMappers.get(event.getPayloadCase());
        } else {
            throw new IllegalArgumentException("Нету подходящего маппера");
        }

        HubEventAvro eventAvro = eventMapper.mapToAvro(event);

        collectorClientProducer.getProducer().send(new ProducerRecord<>(hubsEventsTopic, eventAvro.getHubId(), eventAvro));
    }
}