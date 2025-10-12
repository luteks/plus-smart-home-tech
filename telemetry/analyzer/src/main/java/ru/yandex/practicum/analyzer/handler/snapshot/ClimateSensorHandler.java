package ru.yandex.practicum.analyzer.handler.snapshot;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class ClimateSensorHandler implements SensorHandler {
    @Override
    public String getType() {
        return ClimateSensorAvro.class.getSimpleName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        ClimateSensorAvro sensorAvro = (ClimateSensorAvro) stateAvro.getData();

        return switch (type) {
            case TEMPERATURE -> sensorAvro.getTemperatureC();
            case HUMIDITY -> sensorAvro.getHumidity();
            case CO2LEVEL -> sensorAvro.getCo2Level();
            default -> null;
        };
    }
}