package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.CollectorClientProducer;
import ru.yandex.practicum.model.hub.*;
import ru.yandex.practicum.service.converter.hub.DeviceAddedEventConverter;
import ru.yandex.practicum.service.converter.hub.DeviceRemovedEventConverter;
import ru.yandex.practicum.service.converter.hub.ScenarioAddedEventConverter;
import ru.yandex.practicum.service.converter.hub.ScenarioRemovedEventConverter;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubService {

    private final CollectorClientProducer hubKafkaProducer;

    @Value("${hubEventTopic}")
    private String hubEventTopic;

    public void processHubEvent(HubEvent event) {
        switch (event.getType()) {
            case DEVICE_ADDED:
                new DeviceAddedEventConverter()
                        .sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (DeviceAddedEvent) event);
                break;
            case DEVICE_REMOVED:
                new DeviceRemovedEventConverter()
                        .sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (DeviceRemovedEvent) event);
                break;
            case SCENARIO_ADDED:
                new ScenarioAddedEventConverter()
                        .sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (ScenarioAddedEvent) event);
                break;
            case SCENARIO_REMOVED:
                new ScenarioRemovedEventConverter()
                        .sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (ScenarioRemovedEvent) event);
                break;
            default:
                log.error("Неизвестный тип события для hub: {}", event.getType());
                throw new IllegalArgumentException("Неизвестный тип события hub: " + event.getType());
        }
    }
}