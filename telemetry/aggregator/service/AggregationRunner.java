package ru.yandex.practicum.telemetry.aggregator.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AggregationRunner implements ApplicationRunner {

    private final AggregationStarter starter;

    public AggregationRunner(AggregationStarter starter) {
        this.starter = starter;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread t = new Thread(starter::start);
        t.setName("AggregationThread");
        t.setDaemon(false);
        t.start();
    }
}