package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.CollectorClientProducer;
import ru.yandex.practicum.model.sensors.*;
import ru.yandex.practicum.service.converter.sensors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final CollectorClientProducer sensorKafkaProducer;

    @Value("${sensorEventTopic}")
    private String sensorEventTopic;

    private final ClimateSensorEventConverter climateSensorEventConverter;
    private final LightSensorEventConverter lightSensorEventConverter;
    private final MotionSensorEventConverter motionSensorEventConverter;
    private final SwitchSensorEventConverter switchSensorEventConverter;
    private final TemperatureSensorEventConverter temperatureSensorEventConverter;

    public void processSensorEvent(SensorEvent event) {
        switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT:
                validateEventType(event, ClimateSensorEvent.class);
                climateSensorEventConverter.sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (ClimateSensorEvent) event);
                break;
            case LIGHT_SENSOR_EVENT:
                validateEventType(event, LightSensorEvent.class);
                lightSensorEventConverter.sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (LightSensorEvent) event);
                break;
            case MOTION_SENSOR_EVENT:
                validateEventType(event, MotionSensorEvent.class);
                motionSensorEventConverter.sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (MotionSensorEvent) event);
                break;
            case SWITCH_SENSOR_EVENT:
                validateEventType(event, SwitchSensorEvent.class);
                switchSensorEventConverter.sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (SwitchSensorEvent) event);
                break;
            case TEMPERATURE_SENSOR_EVENT:
                validateEventType(event, TemperatureSensorEvent.class);
                temperatureSensorEventConverter.sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (TemperatureSensorEvent) event);
                break;
            default:
                log.error("Неизвестный тип события для sensor: {}", event.getType());
                throw new IllegalArgumentException("Неизвестный тип события sensor: " + event.getType());
        }
    }

    private void validateEventType(SensorEvent event, Class<?> expectedClass) {
        if (!expectedClass.isInstance(event)) {
            throw new IllegalArgumentException(
                    "Ожидался тип " + expectedClass.getSimpleName() + ", но получен " + event.getClass().getSimpleName());
        }
    }
}