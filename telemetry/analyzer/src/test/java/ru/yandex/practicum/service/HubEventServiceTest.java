package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.service.handlers.HubEventHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubEventServiceTest {

    @Mock
    private HubEventHandler<DeviceAddedEventAvro> deviceAddedHandler;

    @Mock
    private HubEventHandler<DeviceRemovedEventAvro> deviceRemovedHandler;

    @Mock
    private HubEventHandler<ScenarioAddedEventAvro> scenarioAddedHandler;

    @Mock
    private HubEventHandler<ScenarioRemovedEventAvro> scenarioRemovedHandler;

    private HubEventService hubEventService;

    private static final String HUB_ID = "hub-1";
    private static final Instant TIMESTAMP = Instant.parse("2026-11-16T05:00:00Z");

    @BeforeEach
    void setUp() {
        lenient().when(deviceAddedHandler.getPayloadType()).thenReturn(DeviceAddedEventAvro.class);
        lenient().when(deviceRemovedHandler.getPayloadType()).thenReturn(DeviceRemovedEventAvro.class);
        lenient().when(scenarioAddedHandler.getPayloadType()).thenReturn(ScenarioAddedEventAvro.class);
        lenient().when(scenarioRemovedHandler.getPayloadType()).thenReturn(ScenarioRemovedEventAvro.class);

        hubEventService = new HubEventService(Set.of(
                deviceAddedHandler,
                deviceRemovedHandler,
                scenarioAddedHandler,
                scenarioRemovedHandler
        ));
        clearInvocations(
                deviceAddedHandler,
                deviceRemovedHandler,
                scenarioAddedHandler,
                scenarioRemovedHandler
        );
    }

    @Test
    @DisplayName("Передаёт событие добавления устройства в DeviceAddedEventHandler")
    void shouldRouteDeviceAddedEventToAppropriateHandler() {
        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId("sensor-1")
                .setType(DeviceTypeAvro.MOTION_SENSOR)
                .build();
        HubEventAvro event = buildHubEvent(payload);

        hubEventService.process(event);

        verify(deviceAddedHandler).handle(HUB_ID, payload);
        verifyNoInteractions(deviceRemovedHandler, scenarioAddedHandler, scenarioRemovedHandler);
    }

    @Test
    @DisplayName("Передаёт событие удаления устройства в DeviceRemovedEventHandler")
    void shouldRouteDeviceRemovedEventToAppropriateHandler() {
        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId("sensor-1")
                .build();
        HubEventAvro event = buildHubEvent(payload);

        hubEventService.process(event);

        verify(deviceRemovedHandler).handle(HUB_ID, payload);
        verifyNoInteractions(deviceAddedHandler, scenarioAddedHandler, scenarioRemovedHandler);
    }

    @Test
    @DisplayName("Передаёт событие добавления сценария в ScenarioAddedEventHandler")
    void shouldRouteScenarioAddedEventToAppropriateHandler() {
        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName("Вечерний свет")
                .setConditions(Collections.emptyList())
                .setActions(Collections.emptyList())
                .build();
        HubEventAvro event = buildHubEvent(payload);

        hubEventService.process(event);

        verify(scenarioAddedHandler).handle(HUB_ID, payload);
        verifyNoInteractions(deviceAddedHandler, deviceRemovedHandler, scenarioRemovedHandler);
    }

    @Test
    @DisplayName("Передаёт событие удаления сценария в ScenarioRemovedEventHandler")
    void shouldRouteScenarioRemovedEventToAppropriateHandler() {
        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName("Вечерний свет")
                .build();
        HubEventAvro event = buildHubEvent(payload);

        hubEventService.process(event);

        verify(scenarioRemovedHandler).handle(HUB_ID, payload);
        verifyNoInteractions(deviceAddedHandler, deviceRemovedHandler, scenarioAddedHandler);
    }

    @Test
    @DisplayName("Игнорирует событие с пустым payload (null) и не вызывает обработчики")
    void shouldDoNothingWhenPayloadIsNull() {
        HubEventAvro eventWithNullPayload = new HubEventAvro();
        eventWithNullPayload.setHubId(HUB_ID);
        eventWithNullPayload.setTimestamp(TIMESTAMP);

        hubEventService.process(eventWithNullPayload);

        verifyNoInteractions(
                deviceAddedHandler,
                deviceRemovedHandler,
                scenarioAddedHandler,
                scenarioRemovedHandler
        );
    }

    @Test
    @DisplayName("Не падает с ошибкой, если для типа события не зарегистрирован обработчик")
    void shouldNotFailWhenHandlerNotFound() {
        // Создаём сервис только с одним обработчиком
        HubEventService limitedService = new HubEventService(Set.of(deviceAddedHandler));

        DeviceRemovedEventAvro unregisteredPayload = DeviceRemovedEventAvro.newBuilder()
                .setId("sensor-99")
                .build();
        HubEventAvro event = buildHubEvent(unregisteredPayload);

        limitedService.process(event);

        verifyNoInteractions(deviceRemovedHandler);
    }

    @Test
    @DisplayName("Выбрасывает IllegalStateException при попытке зарегистрировать два обработчика для одного типа")
    void shouldThrowExceptionOnDuplicateHandlerTypes() {
        @SuppressWarnings("unchecked")
        HubEventHandler<DeviceAddedEventAvro> duplicateHandler = org.mockito.Mockito.mock(HubEventHandler.class);
        when(duplicateHandler.getPayloadType()).thenReturn(DeviceAddedEventAvro.class);

        Set<HubEventHandler<? extends org.apache.avro.specific.SpecificRecordBase>> duplicateSet =
                Set.of(deviceAddedHandler, duplicateHandler);

        assertThrows(IllegalStateException.class, () -> new HubEventService(duplicateSet));
    }

    private HubEventAvro buildHubEvent(Object payload) {
        return HubEventAvro.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(TIMESTAMP)
                .setPayload(payload)
                .build();
    }
}
