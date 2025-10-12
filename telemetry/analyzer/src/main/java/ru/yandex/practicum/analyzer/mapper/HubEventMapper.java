package ru.yandex.practicum.analyzer.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.analyzer.model.enums.ActionType;
import ru.yandex.practicum.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HubEventMapper {
    public static Action mapToAction(DeviceActionAvro actionAvro) {
        Action action = new Action();
        action.setValue(actionAvro.getValue());
        action.setType(ActionType.valueOf(actionAvro.getType().name()));

        return action;
    }

    public static Condition mapToCondition(ScenarioConditionAvro conditionAvro) {
        Condition condition = new Condition();
        condition.setValue(getValue(conditionAvro.getValue()));
        condition.setType(ConditionType.valueOf(conditionAvro.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(conditionAvro.getOperation().name()));

        return condition;
    }

    public static Scenario mapToScenario(ScenarioAddedEventAvro scenarioAvro, String hubId) {
        Scenario scenario = new Scenario();
        scenario.setName(scenarioAvro.getName());
        scenario.setHubId(hubId);

        Map<String, Condition> conditionMap = scenarioAvro.getConditions().stream().collect(Collectors.toMap(ScenarioConditionAvro::getSensorId, HubEventMapper::mapToCondition));
        Map<String, Action> actionMap = scenarioAvro.getActions().stream().collect(Collectors.toMap(DeviceActionAvro::getSensorId, HubEventMapper::mapToAction));

        scenario.setConditions(conditionMap);
        scenario.setActions(actionMap);

        return scenario;
    }

    public static Sensor mapToSensor(String id, String hubId) {
        Sensor sensor = new Sensor();
        sensor.setId(id);
        sensor.setHubId(hubId);

        return sensor;
    }

    private static Integer getValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return (Boolean) value ? 1 : 0;
        }
    }

}