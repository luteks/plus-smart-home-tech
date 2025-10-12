package ru.yandex.practicum.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.analyzer.processor.SnapshotProcessor;

@Slf4j
@Component
public class AnalyzerStarter {
    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    public AnalyzerStarter(HubEventProcessor hubEventProcessor, SnapshotProcessor snapshotProcessor) {
        this.hubEventProcessor = hubEventProcessor;
        this.snapshotProcessor = snapshotProcessor;
    }

    public void start() {
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");

        hubEventsThread.start();
        snapshotProcessor.start();
    }

}