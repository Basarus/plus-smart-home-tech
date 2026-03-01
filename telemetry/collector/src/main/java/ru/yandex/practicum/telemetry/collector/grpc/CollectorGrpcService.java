package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;

import java.nio.ByteBuffer;
import java.time.Instant;

@GrpcService
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final TelemetryProducer producer;

    public CollectorGrpcService(TelemetryProducer producer) {
        this.producer = producer;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        producer.sendSensorEvent(toAvro(request));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void collectHubEvent(HubEventMessagesProto.HubEventProto request, StreamObserver<Empty> responseObserver) {
        producer.sendHubEvent(toAvro(request));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private SensorEventAvro toAvro(SensorEventProto p) {
        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(p.getId());
        avro.setHubId(p.getHubId());
        avro.setTimestamp(toInstant(p.getTimestamp()));

        Object payload = switch (p.getPayloadCase()) {
            case MOTION_SENSOR -> {
                MotionSensorAvro a = new MotionSensorAvro();
                a.setLinkQuality(p.getMotionSensor().getLinkQuality());
                a.setMotion(p.getMotionSensor().getMotion());
                a.setVoltage(p.getMotionSensor().getVoltage());
                yield a;
            }
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorAvro a = new TemperatureSensorAvro();
                a.setTemperatureC(p.getTemperatureSensor().getTemperatureC());
                a.setTemperatureF(p.getTemperatureSensor().getTemperatureF());
                yield a;
            }
            case LIGHT_SENSOR -> {
                LightSensorAvro a = new LightSensorAvro();
                a.setLinkQuality(p.getLightSensor().getLinkQuality());
                a.setLuminosity(p.getLightSensor().getLuminosity());
                yield a;
            }
            case CLIMATE_SENSOR -> {
                ClimateSensorAvro a = new ClimateSensorAvro();
                a.setTemperatureC(p.getClimateSensor().getTemperatureC());
                a.setHumidity(p.getClimateSensor().getHumidity());
                a.setCo2Level(p.getClimateSensor().getCo2Level());
                yield a;
            }
            case SWITCH_SENSOR -> {
                SwitchSensorAvro a = new SwitchSensorAvro();
                a.setState(p.getSwitchSensor().getState());
                yield a;
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor payload is not set");
        };

        avro.setPayload(payload);
        return avro;
    }

    private HubEventAvro toAvro(HubEventMessagesProto.HubEventProto p) {
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(p.getHubId());
        avro.setTimestamp(toInstant(p.getTimestamp()));

        byte[] payloadBytes = switch (p.getPayloadCase()) {
            case DEVICE_ADDED -> p.getDeviceAdded().toByteArray();
            case DEVICE_REMOVED -> p.getDeviceRemoved().toByteArray();
            case SCENARIO_ADDED -> p.getScenarioAdded().toByteArray();
            case SCENARIO_REMOVED -> p.getScenarioRemoved().toByteArray();
            case DEVICE_ACTION_REQUEST -> p.getDeviceActionRequest().toByteArray();
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub payload is not set");
        };

        avro.setPayload(ByteBuffer.wrap(payloadBytes));
        return avro;
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }
}