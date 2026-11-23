package ru.yandex.practicum.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.api.HubRouterSender;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.jpa.ScenarioRepository;
import ru.yandex.practicum.jpa.model.Action;
import ru.yandex.practicum.jpa.model.Condition;
import ru.yandex.practicum.jpa.model.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotsServiceTest {

    @Mock
    private HubRouterSender hubRouterSender;

    @Mock
    private ScenarioRepository scenarioRepository;

    @InjectMocks
    private SnapshotsService snapshotsService;

    private static final String HUB_ID = "hub-1";
    private static final Instant TIMESTAMP = Instant.parse("2026-11-16T05:00:00Z");

    @Test
    @DisplayName("Не обращается к БД и не шлёт команды, если снапшот пустой")
    void shouldIgnoreEmptySnapshot() {
        SensorsSnapshotAvro emptySnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(TIMESTAMP)
                .setSensorsState(Collections.emptyMap())
                .build();

        snapshotsService.process(emptySnapshot);

        verifyNoInteractions(scenarioRepository, hubRouterSender);
    }

    @Test
    @DisplayName("Не отправляет команды, если для хаба нет сценариев")
    void shouldDoNothingWhenNoScenariosFound() {
        SensorsSnapshotAvro snapshot = buildSnapshot(Map.of(
                "sensor-1", buildState(TemperatureSensorAvro.newBuilder()
                        .setId("sensor-1")
                        .setHubId(HUB_ID)
                        .setTimestamp(TIMESTAMP)
                        .setTemperatureC(25)
                        .setTemperatureF(77)
                        .build())
        ));
        when(scenarioRepository.findByHubId(HUB_ID)).thenReturn(Collections.emptyList());

        snapshotsService.process(snapshot);

        verify(scenarioRepository).findByHubId(HUB_ID);
        verifyNoInteractions(hubRouterSender);
    }

    @Test
    @DisplayName("Срабатывает сценарий с операцией GREATER_THAN и отправляет действие с value")
    void shouldTriggerScenarioWhenGreaterThanConditionMet() {
        String sensorId = "climate-1";
        String targetDeviceId = "conditioner-1";

        SensorsSnapshotAvro snapshot = buildSnapshot(Map.of(
                sensorId, buildState(ClimateSensorAvro.newBuilder()
                        .setTemperatureC(28)
                        .setHumidity(60)
                        .setCo2Level(800)
                        .build())
        ));

        Scenario scenario = Scenario.builder()
                .id(1L)
                .hubId(HUB_ID)
                .name("Охлаждение спальни")
                .conditions(Map.of(
                        sensorId, Condition.builder()
                                .type(ConditionTypeAvro.TEMPERATURE)
                                .operation(ConditionOperationAvro.GREATER_THAN)
                                .value(25)
                                .build()
                ))
                .actions(Map.of(
                        targetDeviceId, Action.builder()
                                .type(ActionTypeAvro.SET_VALUE)
                                .value(21)
                                .build()
                ))
                .build();

        when(scenarioRepository.findByHubId(HUB_ID)).thenReturn(List.of(scenario));

        snapshotsService.process(snapshot);

        ArgumentCaptor<DeviceActionRequest> captor = ArgumentCaptor.forClass(DeviceActionRequest.class);
        verify(hubRouterSender, times(1)).sendAction(captor.capture());

        DeviceActionRequest sentRequest = captor.getValue();
        assertEquals(HUB_ID, sentRequest.getHubId());
        assertEquals("Охлаждение спальни", sentRequest.getScenarioName());
        assertEquals(targetDeviceId, sentRequest.getAction().getSensorId());
        assertEquals(ActionTypeProto.SET_VALUE, sentRequest.getAction().getType());
        assertTrue(sentRequest.getAction().hasValue());
        assertEquals(21, sentRequest.getAction().getValue());
        assertEquals(TIMESTAMP.getEpochSecond(), sentRequest.getTimestamp().getSeconds());
    }

    @Test
    @DisplayName("Срабатывает сценарий с булевым датчиком движения (EQUALS = 1) и действием без value")
    void shouldTriggerMotionScenarioWithoutActionValue() {
        String motionSensorId = "motion-1";
        String lampId = "lamp-1";

        SensorsSnapshotAvro snapshot = buildSnapshot(Map.of(
                motionSensorId, buildState(MotionSensorAvro.newBuilder()
                        .setMotion(true)
                        .setLinkQuality(90)
                        .setVoltage(3300)
                        .build())
        ));

        Scenario scenario = Scenario.builder()
                .id(2L)
                .hubId(HUB_ID)
                .name("Свет в коридоре")
                .conditions(Map.of(
                        motionSensorId, Condition.builder()
                                .type(ConditionTypeAvro.MOTION)
                                .operation(ConditionOperationAvro.EQUALS)
                                .value(1)
                                .build()
                ))
                .actions(Map.of(
                        lampId, Action.builder()
                                .type(ActionTypeAvro.ACTIVATE)
                                .value(null)
                                .build()
                ))
                .build();

        when(scenarioRepository.findByHubId(HUB_ID)).thenReturn(List.of(scenario));

        snapshotsService.process(snapshot);

        ArgumentCaptor<DeviceActionRequest> captor = ArgumentCaptor.forClass(DeviceActionRequest.class);
        verify(hubRouterSender).sendAction(captor.capture());

        DeviceActionRequest sentRequest = captor.getValue();
        assertEquals(ActionTypeProto.ACTIVATE, sentRequest.getAction().getType());
        assertFalse(sentRequest.getAction().hasValue());
    }

    @Test
    @DisplayName("Сценарий с несколькими условиями НЕ срабатывает, если хотя бы одно условие ложно")
    void shouldNotTriggerWhenOneOfMultipleConditionsFails() {
        String lightSensorId = "light-1";
        String switchSensorId = "switch-1";

        SensorsSnapshotAvro snapshot = buildSnapshot(Map.of(
                lightSensorId, buildState(LightSensorAvro.newBuilder()
                        .setLuminosity(100) // условие < 200 выполнено
                        .setLinkQuality(85)
                        .build()),
                switchSensorId, buildState(SwitchSensorAvro.newBuilder()
                        .setState(false) // условие == 1 НЕ выполнено
                        .build())
        ));

        Scenario scenario = Scenario.builder()
                .id(3L)
                .hubId(HUB_ID)
                .name("Вечерний свет")
                .conditions(Map.of(
                        lightSensorId, Condition.builder()
                                .type(ConditionTypeAvro.LUMINOSITY)
                                .operation(ConditionOperationAvro.LOWER_THAN)
                                .value(200)
                                .build(),
                        switchSensorId, Condition.builder()
                                .type(ConditionTypeAvro.SWITCH)
                                .operation(ConditionOperationAvro.EQUALS)
                                .value(1)
                                .build()
                ))
                .actions(Map.of(
                        "lamp-2", Action.builder()
                                .type(ActionTypeAvro.INVERSE)
                                .build()
                ))
                .build();

        when(scenarioRepository.findByHubId(HUB_ID)).thenReturn(List.of(scenario));

        snapshotsService.process(snapshot);

        verify(hubRouterSender, never()).sendAction(any());
    }

    @Test
    @DisplayName("Сценарий НЕ срабатывает, если в снапшоте отсутствуют данные по нужному датчику")
    void shouldNotTriggerWhenSensorMissingInSnapshot() {
        SensorsSnapshotAvro snapshot = buildSnapshot(Map.of(
                "other-sensor", buildState(TemperatureSensorAvro.newBuilder()
                        .setId("other-sensor")
                        .setHubId(HUB_ID)
                        .setTimestamp(TIMESTAMP)
                        .setTemperatureC(20)
                        .setTemperatureF(68)
                        .build())
        ));

        Scenario scenario = Scenario.builder()
                .id(4L)
                .hubId(HUB_ID)
                .name("Проветривание")
                .conditions(Map.of(
                        "missing-climate-sensor", Condition.builder()
                                .type(ConditionTypeAvro.CO2LEVEL)
                                .operation(ConditionOperationAvro.GREATER_THAN)
                                .value(1000)
                                .build()
                ))
                .actions(Map.of(
                        "vent-1", Action.builder()
                                .type(ActionTypeAvro.DEACTIVATE)
                                .build()
                ))
                .build();

        when(scenarioRepository.findByHubId(HUB_ID)).thenReturn(List.of(scenario));
        snapshotsService.process(snapshot);
        verify(hubRouterSender, never()).sendAction(any());
    }

    @Test
    @DisplayName("Корректно извлекает температуру из TemperatureSensorAvro (не только из ClimateSensorAvro)")
    void shouldExtractTemperatureFromStandaloneTemperatureSensor() {
        String tempSensorId = "temp-1";

        SensorsSnapshotAvro snapshot = buildSnapshot(Map.of(
                tempSensorId, buildState(TemperatureSensorAvro.newBuilder()
                        .setId(tempSensorId)
                        .setHubId(HUB_ID)
                        .setTimestamp(TIMESTAMP)
                        .setTemperatureC(18)
                        .setTemperatureF(64)
                        .build())
        ));

        Scenario scenario = Scenario.builder()
                .id(5L)
                .hubId(HUB_ID)
                .name("Обогрев")
                .conditions(Map.of(
                        tempSensorId, Condition.builder()
                                .type(ConditionTypeAvro.TEMPERATURE)
                                .operation(ConditionOperationAvro.LOWER_THAN)
                                .value(20)
                                .build()
                ))
                .actions(Map.of(
                        "heater-1", Action.builder()
                                .type(ActionTypeAvro.ACTIVATE)
                                .build()
                ))
                .build();

        when(scenarioRepository.findByHubId(HUB_ID)).thenReturn(List.of(scenario));

        snapshotsService.process(snapshot);

        verify(hubRouterSender, times(1)).sendAction(any());
    }

    private SensorsSnapshotAvro buildSnapshot(Map<String, SensorStateAvro> states) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(TIMESTAMP)
                .setSensorsState(states)
                .build();
    }
    private SensorStateAvro buildState(Object payload) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(TIMESTAMP)
                .setData(payload)
                .build();
    }
}