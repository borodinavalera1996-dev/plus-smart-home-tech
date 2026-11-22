package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.handlers.HubEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HubEventService {

    private final Map<Class<? extends SpecificRecordBase>, HubEventHandler<? extends SpecificRecordBase>> handlers;

    public HubEventService(Set<HubEventHandler<? extends SpecificRecordBase>> handlerSet) {
        this.handlers = handlerSet.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getPayloadType,
                        Function.identity()
                ));
    }

    public void process(HubEventAvro event) {
        Object payload = event.getPayload();
        if (payload == null) {
            log.warn("Получено событие хаба {} с пустым payload", event.getHubId());
            return;
        }

        HubEventHandler<? extends SpecificRecordBase> handler = handlers.get(payload.getClass());
        if (handler == null) {
            log.warn("Не найден обработчик для события типа: {}", payload.getClass().getName());
            return;
        }

        invokeHandler(handler, event.getHubId(), payload);
    }

    private <T extends SpecificRecordBase> void invokeHandler(
            HubEventHandler<T> handler,
            String hubId,
            Object payload
    ) {
        handler.handle(hubId, handler.getPayloadType().cast(payload));
    }
}
