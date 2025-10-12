package ru.yandex.practicum.analyzer.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HubRouterClient {
    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub hubRouterController;

    public void send(Scenario scenario) {
        Map<String, Action> actionMap = scenario.getActions();

        for (String sensorId : actionMap.keySet()) {
            DeviceActionRequest request = mapToDeviceActionRequest(sensorId, actionMap.get(sensorId), scenario);
            hubRouterController.handleDeviceAction(request);
        }
    }

    public void send(List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            send(scenario);
        }
    }

    private DeviceActionRequest mapToDeviceActionRequest(String sensorId, Action action, Scenario scenario) {
        DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(Instant.now().getEpochSecond())
                .setNanos(Instant.now().getNano())
                .build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(scenario.getHubId())
                .setScenarioName(scenario.getName())
                .setAction(deviceAction)
                .setTimestamp(timestamp)
                .build();

        return request;
    }
}