package ru.yandex.practicum.analyzer.handler.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.client.HubRouterClient;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SensorsSnapshotHandler {

    private final HubRouterClient hubRouterClient;
    private final ScenarioRepository scenarioRepository;
    private final Map<String, SensorHandler> sensorHandlers;

    public SensorsSnapshotHandler(HubRouterClient hubRouterClient, ScenarioRepository scenarioRepository, List<SensorHandler> sensorHandlers) {
        this.hubRouterClient = hubRouterClient;
        this.scenarioRepository = scenarioRepository;
        this.sensorHandlers = sensorHandlers.stream().collect(Collectors.toMap(SensorHandler::getType, Function.identity()));
    }

    public void handle(SensorsSnapshotAvro snapshot) {
        List<Scenario> scenarios = scenarioRepository.findAllByHubId(snapshot.getHubId());

        if (scenarios.isEmpty()) {
            throw new IllegalArgumentException("Сценарии хаба не найдены");
        }

        List<Scenario> validScenarios = scenarios.stream()
                .filter(scenario -> validateScenarioConditions(scenario, snapshot))
                .toList();

        hubRouterClient.send(validScenarios);
    }

    private Boolean validateScenarioConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        Map<String, Condition> conditions = scenario.getConditions();
        Map<String, SensorStateAvro> sensorStates = snapshot.getSensorsState();

        if (snapshot == null || snapshot.getSensorsState().isEmpty()) {
            return false;
        }

        return conditions.keySet().stream().allMatch(sensorId -> validateScenarioConditions(conditions.get(sensorId), sensorStates.get(sensorId)));
    }

    private Boolean validateScenarioConditions(Condition condition, SensorStateAvro sensorState) {
        if (sensorState == null) {
            return false;
        }

        SensorHandler handler;
        if (sensorHandlers.containsKey(sensorState.getData().getClass().getSimpleName())) {
            handler = sensorHandlers.get(sensorState.getData().getClass().getSimpleName());
        } else {
            throw new IllegalArgumentException("Подходящего обработчика не найдено: " + sensorState.getData().getClass().getSimpleName());
        }

        Integer value = handler.handleToValue(sensorState, condition.getType());

        if (value == null) {
            return false;
        }

        return getConditionOperation(condition, value);
    }

    private Boolean getConditionOperation(Condition condition, Integer value) {
        return switch (condition.getOperation()) {
            case EQUALS -> value.equals(condition.getValue());
            case GREATER_THAN -> value > condition.getValue();
            case LOWER_THAN -> value < condition.getValue();
        };
    }

}