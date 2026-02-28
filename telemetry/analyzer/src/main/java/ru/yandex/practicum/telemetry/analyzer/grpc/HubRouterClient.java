package ru.yandex.practicum.telemetry.analyzer.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto;

@Service
public class HubRouterClient {

    private final HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterClient(@GrpcClient("hub-router") HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void handleDeviceAction(HubEventMessagesProto.DeviceActionRequest request) {
        hubRouterClient.handleDeviceAction(request);
    }
}