package ru.yandex.practicum.producer;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Component
public class EventDataProducer {

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collectorStub;

    private final String HUB_ID = "hub-1";

    // --- Конфигурация датчиков ---
    @Value("${sensor.motionSensors[0].id}")
    private String motionSensorId1;
    @Value("${sensor.motionSensors[0].linkQuality.minValue}")
    private int motionSensorLinkQualityMin1;
    @Value("${sensor.motionSensors[0].linkQuality.maxValue}")
    private int motionSensorLinkQualityMax1;
    @Value("${sensor.motionSensors[0].voltage.minValue}")
    private int motionSensorVoltageMin1;
    @Value("${sensor.motionSensors[0].voltage.maxValue}")
    private int motionSensorVoltageMax1;

    @Value("${sensor.motionSensors[1].id}")
    private String motionSensorId2;
    @Value("${sensor.motionSensors[1].linkQuality.minValue}")
    private int motionSensorLinkQualityMin2;
    @Value("${sensor.motionSensors[1].linkQuality.maxValue}")
    private int motionSensorLinkQualityMax2;
    @Value("${sensor.motionSensors[1].voltage.minValue}")
    private int motionSensorVoltageMin2;
    @Value("${sensor.motionSensors[1].voltage.maxValue}")
    private int motionSensorVoltageMax2;

    @Value("${sensor.switchSensors[0].id}")
    private String switchSensorId1;

    @Value("${sensor.temperatureSensors[0].id}")
    private String temperatureSensorId1;
    @Value("${sensor.temperatureSensors[0].temperature.minValue}")
    private int temperatureSensorMin1;
    @Value("${sensor.temperatureSensors[0].temperature.maxValue}")
    private int temperatureSensorMax1;

    @Value("${sensor.temperatureSensors[1].id}")
    private String temperatureSensorId2;
    @Value("${sensor.temperatureSensors[1].temperature.minValue}")
    private int temperatureSensorMin2;
    @Value("${sensor.temperatureSensors[1].temperature.maxValue}")
    private int temperatureSensorMax2;

    @Value("${sensor.lightSensors[0].id}")
    private String lightSensorId1;
    @Value("${sensor.lightSensors[0].luminosity.minValue}")
    private int lightSensorLuminosityMin1;
    @Value("${sensor.lightSensors[0].luminosity.maxValue}")
    private int lightSensorLuminosityMax1;

    @Value("${sensor.lightSensors[1].id}")
    private String lightSensorId2;
    @Value("${sensor.lightSensors[1].luminosity.minValue}")
    private int lightSensorLuminosityMin2;
    @Value("${sensor.lightSensors[1].luminosity.maxValue}")
    private int lightSensorLuminosityMax2;

    @Value("${sensor.climateSensors[0].id}")
    private String climateSensorId1;
    @Value("${sensor.climateSensors[0].temperature.minValue}")
    private int climateSensorTemperatureMin1;
    @Value("${sensor.climateSensors[0].temperature.maxValue}")
    private int climateSensorTemperatureMax1;
    @Value("${sensor.climateSensors[0].humidity.minValue}")
    private int climateSensorHumidityMin1;
    @Value("${sensor.climateSensors[0].humidity.maxValue}")
    private int climateSensorHumidityMax1;
    @Value("${sensor.climateSensors[0].co2Level.minValue}")
    private int climateSensorCo2LevelMin1;
    @Value("${sensor.climateSensors[0].co2Level.maxValue}")
    private int climateSensorCo2LevelMax1;

    @Value("${sensor.climateSensors[1].id}")
    private String climateSensorId2;
    @Value("${sensor.climateSensors[1].temperature.minValue}")
    private int climateSensorTemperatureMin2;
    @Value("${sensor.climateSensors[1].temperature.maxValue}")
    private int climateSensorTemperatureMax2;
    @Value("${sensor.climateSensors[1].humidity.minValue}")
    private int climateSensorHumidityMin2;
    @Value("${sensor.climateSensors[1].humidity.maxValue}")
    private int climateSensorHumidityMax2;
    @Value("${sensor.climateSensors[1].co2Level.minValue}")
    private int climateSensorCo2LevelMin2;
    @Value("${sensor.climateSensors[1].co2Level.maxValue}")
    private int climateSensorCo2LevelMax2;

    private final Random random = new Random();

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    private void sendData() {
        int randomSensorId = random.nextInt(9) + 1;

        switch (randomSensorId) {
            case 1:
                sendEvent(createMotionSensorEvent(
                        motionSensorId1,
                        getRandomSensorValueInRange(motionSensorLinkQualityMin1, motionSensorLinkQualityMax1),
                        getRandomSensorValueInRange(motionSensorVoltageMin1, motionSensorVoltageMax1),
                        random.nextBoolean()
                ));
                break;
            case 2:
                sendEvent(createMotionSensorEvent(
                        motionSensorId2,
                        getRandomSensorValueInRange(motionSensorLinkQualityMin2, motionSensorLinkQualityMax2),
                        getRandomSensorValueInRange(motionSensorVoltageMin2, motionSensorVoltageMax2),
                        random.nextBoolean()
                ));
                break;
            case 3:
                sendEvent(createSwitchSensorEvent(
                        switchSensorId1,
                        random.nextBoolean()
                ));
                break;
            case 4:
                sendEvent(createTemperatureSensorEvent(
                        temperatureSensorId1,
                        getRandomSensorValueInRange(temperatureSensorMin1, temperatureSensorMax1)
                ));
                break;
            case 5:
                sendEvent(createTemperatureSensorEvent(
                        temperatureSensorId2,
                        getRandomSensorValueInRange(temperatureSensorMin2, temperatureSensorMax2)
                ));
                break;
            case 6:
                sendEvent(createLightSensorEvent(
                        lightSensorId1,
                        getRandomSensorValueInRange(lightSensorLuminosityMin1, lightSensorLuminosityMax1),
                        random.nextInt(100) + 1
                ));
                break;
            case 7:
                sendEvent(createLightSensorEvent(
                        lightSensorId2,
                        getRandomSensorValueInRange(lightSensorLuminosityMin2, lightSensorLuminosityMax2),
                        random.nextInt(100) + 1
                ));
                break;
            case 8:
                sendEvent(createClimateSensorEvent(
                        climateSensorId1,
                        getRandomSensorValueInRange(climateSensorTemperatureMin1, climateSensorTemperatureMax1),
                        getRandomSensorValueInRange(climateSensorHumidityMin1, climateSensorHumidityMax1),
                        getRandomSensorValueInRange(climateSensorCo2LevelMin1, climateSensorCo2LevelMax1)
                ));
                break;
            case 9:
                sendEvent(createClimateSensorEvent(
                        climateSensorId2,
                        getRandomSensorValueInRange(climateSensorTemperatureMin2, climateSensorTemperatureMax2),
                        getRandomSensorValueInRange(climateSensorHumidityMin2, climateSensorHumidityMax2),
                        getRandomSensorValueInRange(climateSensorCo2LevelMin2, climateSensorCo2LevelMax2)
                ));
                break;
            default:
                break; // на всякий случай
        }
    }

    private void sendEvent(SensorEventProto event) {
        log.info("Отправляю данные: {}", event.toString());
        collectorStub.collectSensorEvent(event);
    }


    private SensorEventProto createMotionSensorEvent(
            String sensorId,
            int linkQuality,
            int voltage,
            boolean motion
    ) {
        Instant ts = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensorId)
                .setHubId(HUB_ID)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(ts.getEpochSecond())
                        .setNanos(ts.getNano())
                )
                .setMotionSensorEvent(
                        ru.yandex.practicum.grpc.telemetry.event.MotionSensorEvent.newBuilder()
                                .setLinkQuality(linkQuality)
                                .setMotion(motion)
                                .setVoltage(voltage)
                                .build()
                )
                .build();
    }

    private SensorEventProto createClimateSensorEvent(
            String sensorId,
            int temperatureC,
            int humidity,
            int co2Level
    ) {
        Instant ts = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensorId)
                .setHubId(HUB_ID)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(ts.getEpochSecond())
                        .setNanos(ts.getNano())
                )
                .setClimateSensorEvent(
                        ru.yandex.practicum.grpc.telemetry.event.ClimateSensorEvent.newBuilder()
                                .setTemperatureC(temperatureC)
                                .setHumidity(humidity)
                                .setCo2Level(co2Level)
                                .build()
                )
                .build();
    }

    private SensorEventProto createLightSensorEvent(
            String sensorId,
            int luminosity,
            int linkQuality
    ) {
        Instant ts = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensorId)
                .setHubId(HUB_ID)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(ts.getEpochSecond())
                        .setNanos(ts.getNano())
                )
                .setLightSensorEvent(
                        ru.yandex.practicum.grpc.telemetry.event.LightSensorEvent.newBuilder()
                                .setLuminosity(luminosity)
                                .setLinkQuality(linkQuality)
                                .build()
                )
                .build();
    }

    private SensorEventProto createSwitchSensorEvent(
            String sensorId,
            boolean state
    ) {
        Instant ts = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensorId)
                .setHubId(HUB_ID)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(ts.getEpochSecond())
                        .setNanos(ts.getNano())
                )
                .setSwitchSensorEvent(
                        ru.yandex.practicum.grpc.telemetry.event.SwitchSensorEvent.newBuilder()
                                .setState(state)
                                .build()
                )
                .build();
    }

    private SensorEventProto createTemperatureSensorEvent(
            String sensorId,
            int temperatureCelsius
    ) {
        int temperatureFahrenheit = (int) (temperatureCelsius * 1.8 + 32);
        Instant ts = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensorId)
                .setHubId(HUB_ID)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(ts.getEpochSecond())
                        .setNanos(ts.getNano())
                )
                .setTemperatureSensorEvent(
                        ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorEvent.newBuilder()
                                .setTemperatureC(temperatureCelsius)
                                .setTemperatureF(temperatureFahrenheit)
                                .build()
                )
                .build();
    }

    private int getRandomSensorValueInRange(int min, int max) {
        return min + (int) (Math.random() * (max - min));
    }
}