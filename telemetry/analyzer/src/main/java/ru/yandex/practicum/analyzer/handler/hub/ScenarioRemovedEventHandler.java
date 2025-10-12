package ru.yandex.practicum.analyzer.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    public String getHubEventType() {
        return ScenarioRemovedEventAvro.class.getSimpleName();
    }

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) event.getPayload();
        scenarioRepository.deleteByHubIdAndName(event.getHubId(), payload.getName());
    }
}