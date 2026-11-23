package ru.yandex.practicum.service.handlers;

import org.apache.avro.specific.SpecificRecordBase;

public interface HubEventHandler<T extends SpecificRecordBase> {
    Class<T> getPayloadType();
    void handle(String hubId, T payload);
}
