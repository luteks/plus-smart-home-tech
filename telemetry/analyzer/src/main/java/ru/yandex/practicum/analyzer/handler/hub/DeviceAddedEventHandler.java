package ru.yandex.practicum.analyzer.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.mapper.HubEventMapper;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public String getHubEventType() {
        return DeviceAddedEventAvro.class.getSimpleName();
    }

    @Override
    public void handle(HubEventAvro event) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) event.getPayload();

        Optional<Sensor> oldSensor = sensorRepository.findByIdAndHubId(payload.getId(), event.getHubId());

        if (oldSensor.isEmpty()) {
            Sensor sensor = HubEventMapper.mapToSensor(payload.getId(), event.getHubId());
            sensorRepository.save(sensor);
        } else {
            log.info("Устройство уже зарегистрировано: {}", oldSensor.get());
        }
    }
}