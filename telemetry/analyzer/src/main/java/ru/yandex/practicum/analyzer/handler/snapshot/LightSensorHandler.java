package ru.yandex.practicum.analyzer.handler.snapshot;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class LightSensorHandler implements SensorHandler {
    @Override
    public String getType() {
        return LightSensorAvro.class.getSimpleName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        LightSensorAvro sensorAvro = (LightSensorAvro) stateAvro.getData();

        return switch (type) {
            case LUMINOSITY -> sensorAvro.getLuminosity();
            default -> null;
        };
    }
}