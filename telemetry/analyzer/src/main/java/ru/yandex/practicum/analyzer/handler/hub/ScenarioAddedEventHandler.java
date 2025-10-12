package ru.yandex.practicum.analyzer.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.mapper.HubEventMapper;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    @Override
    public String getHubEventType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) event.getPayload();
        checkForSensors(payload.getConditions(), payload.getActions(), event.getHubId());

        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(event.getHubId(), payload.getName());
        scenarioOpt.ifPresent(oldScenario -> scenarioRepository.deleteByHubIdAndName(oldScenario.getHubId(), oldScenario.getName()));
        scenarioRepository.flush();

        Scenario scenario = HubEventMapper.mapToScenario(payload, event.getHubId());
        scenarioRepository.save(scenario);
    }

    private void checkForSensors(List<ScenarioConditionAvro> conditions, List<DeviceActionAvro> actions, String hubId) {
        List<String> conditionSensorIds = conditions.stream().map(ScenarioConditionAvro::getSensorId).toList();
        List<String> actionSensorIds = actions.stream().map(DeviceActionAvro::getSensorId).toList();

        if (!sensorRepository.existsAllByIdInAndHubId(conditionSensorIds, hubId)) {
            throw new IllegalArgumentException("Устройства не найдены");
        }

        if (!sensorRepository.existsAllByIdInAndHubId(actionSensorIds, hubId)) {
            throw new IllegalArgumentException("Устройства не найдены");
        }
    }
}