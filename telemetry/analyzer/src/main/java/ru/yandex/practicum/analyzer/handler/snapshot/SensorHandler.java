package ru.yandex.practicum.analyzer.handler.snapshot;

import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public interface SensorHandler {
    Integer handleToValue(SensorStateAvro stateAvro, ConditionType type);

    String getType();
}