package ru.yandex.practicum.telemetry.analyzer.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Service
public class HubRouterClient {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub client;

    public HubRouterClient(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub client) {
        this.client = client;
    }

    public void handleDeviceAction(HubEventProto.DeviceActionRequest request) {
        client.handleDeviceAction(request);
    }
}