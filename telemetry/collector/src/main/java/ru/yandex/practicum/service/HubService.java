package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final DeviceAddedEventConverter deviceAddedEventConverter;
    private final DeviceRemovedEventConverter deviceRemovedEventConverter;
    private final ScenarioAddedEventConverter scenarioAddedEventConverter;
    private final ScenarioRemovedEventConverter scenarioRemovedEventConverter;

    public void processHubEvent(HubEvent event) {
        switch (event.getType()) {
            case DEVICE_ADDED:
                validateEventType(event, DeviceAddedEvent.class);
                deviceAddedEventConverter.sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (DeviceAddedEvent) event);
                break;
            case DEVICE_REMOVED:
                validateEventType(event, DeviceRemovedEvent.class);
                deviceRemovedEventConverter.sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (DeviceRemovedEvent) event);
                break;
            case SCENARIO_ADDED:
                validateEventType(event, ScenarioAddedEvent.class);
                scenarioAddedEventConverter.sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (ScenarioAddedEvent) event);
                break;
            case SCENARIO_REMOVED:
                validateEventType(event, ScenarioRemovedEvent.class);
                scenarioRemovedEventConverter.sendEvent(hubKafkaProducer, hubEventTopic, event.getHubId(), (ScenarioRemovedEvent) event);
                break;
            default:
                log.error("Неизвестный тип события для hub: {}", event.getType());
                throw new IllegalArgumentException("Неизвестный тип события hub: " + event.getType());
        }
    }

    private void validateEventType(HubEvent event, Class<?> expectedClass) {
        if (!expectedClass.isInstance(event)) {
            throw new IllegalArgumentException(
                    "Ожидался тип " + expectedClass.getSimpleName() + ", но получен " + event.getClass().getSimpleName());
        }
    }
}