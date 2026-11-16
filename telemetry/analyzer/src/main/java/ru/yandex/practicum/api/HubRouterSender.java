package ru.yandex.practicum.api;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;

@Slf4j
@Service
public class HubRouterSender {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void sendAction(DeviceActionRequest request) {
        try {
            log.info("Отправка действия в Hub Router: hubId={}, scenario={}, sensorId={}, type={}",
                    request.getHubId(),
                    request.getScenarioName(),
                    request.getAction().getSensorId(),
                    request.getAction().getType());

            hubRouterClient.handleDeviceAction(request);

            log.info("Действие успешно принято Hub Router");
        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC при отправке действия в Hub Router [status={}]: {}",
                    e.getStatus(), e.getMessage(), e);
        }
    }
}
