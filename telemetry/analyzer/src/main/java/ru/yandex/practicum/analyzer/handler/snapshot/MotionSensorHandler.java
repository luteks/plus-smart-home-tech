package ru.yandex.practicum.analyzer.handler.snapshot;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class MotionSensorHandler implements SensorHandler {
    @Override
    public String getType() {
        return MotionSensorAvro.class.getSimpleName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        MotionSensorAvro sensorAvro = (MotionSensorAvro) stateAvro.getData();

        return switch (type) {
            case MOTION -> sensorAvro.getMotion() ? 1 : 0;
            default -> null;
        };
    }
}