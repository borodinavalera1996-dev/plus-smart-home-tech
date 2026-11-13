package ru.yandex.practicum.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.hub.CollectHubEventRequest;
import ru.yandex.practicum.model.sensor.CollectSensorEventRequest;
import ru.yandex.practicum.service.HubService;
import ru.yandex.practicum.service.SensorService;

@RestController
@RequiredArgsConstructor
public class EventsApiController implements EventsApi {

    private final HubService hubService;
    private final SensorService sensorService;

    @Override
    public ResponseEntity<Void> collectHubEvent(CollectHubEventRequest request) {
        hubService.processHubEvent(request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> collectSensorEvent(CollectSensorEventRequest request) {
        sensorService.processSensorEvent(request);
        return ResponseEntity.ok().build();
    }
}

