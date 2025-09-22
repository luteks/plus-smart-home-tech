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

    public void processSensorEvent(SensorEvent event) {
        switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT:
                new ClimateSensorEventConverter()
                        .sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (ClimateSensorEvent) event);
                break;
            case LIGHT_SENSOR_EVENT:
                new LightSensorEventConverter()
                        .sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (LightSensorEvent) event);
                break;
            case MOTION_SENSOR_EVENT:
                new MotionSensorEventConverter()
                        .sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (MotionSensorEvent) event);
                break;
            case SWITCH_SENSOR_EVENT:
                new SwitchSensorEventConverter()
                        .sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (SwitchSensorEvent) event);
                break;
            case TEMPERATURE_SENSOR_EVENT:
                new TemperatureSensorEventConverter()
                        .sendEvent(sensorKafkaProducer, sensorEventTopic, event.getHubId(), (TemperatureSensorEvent) event);
                break;
            default:
                log.error("Неизвестный тип события для sensor: {}", event.getType());
                throw new IllegalArgumentException("Неизвестный тип события sensor: " + event.getType());
        }
    }
}