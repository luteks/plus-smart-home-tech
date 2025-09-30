package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Slf4j
@Component
public class ClimateSensorEventMapper extends BaseSensorEventMapper<ClimateSensorAvro> {
    @Override
    protected ClimateSensorAvro mapToAvroPayload(SensorEventProto event) {
        ClimateSensorEvent sensorEvent = event.getClimateSensorEvent();

        log.info("Маппинг события {} - результат: {}", ClimateSensorEvent.class.getSimpleName(), sensorEvent);

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(sensorEvent.getTemperatureC())
                .setHumidity(sensorEvent.getHumidity())
                .setCo2Level(sensorEvent.getCo2Level())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getSensorEventType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }
}