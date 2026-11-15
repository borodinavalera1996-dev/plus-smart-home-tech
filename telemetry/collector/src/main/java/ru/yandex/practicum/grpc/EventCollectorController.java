package ru.yandex.practicum.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.hub.CollectHubEventRequest;
import ru.yandex.practicum.model.sensor.CollectSensorEventRequest;
import ru.yandex.practicum.service.HubService;
import ru.yandex.practicum.service.SensorService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventCollectorController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final HubService hubService;
    private final SensorService sensorService;
    private final HubEventProtoMapper hubEventProtoMapper;
    private final SensorEventProtoMapper sensorEventProtoMapper;


    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получено событие от датчика: id={}, hubId={}", request.getId(), request.getHubId());
            CollectSensorEventRequest model = sensorEventProtoMapper.toModel(request);
            sensorService.processSensorEvent(model);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события от датчика: {}", request, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получено событие от хаба: hubId={}, payloadCase={}",
                    request.getHubId(), request.getPayloadCase());
            CollectHubEventRequest eventRequest = hubEventProtoMapper.toModel(request);
            hubService.processHubEvent(eventRequest);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события от хаба: {}", request, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
