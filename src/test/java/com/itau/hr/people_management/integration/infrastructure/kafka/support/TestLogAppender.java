package com.itau.hr.people_management.integration.infrastructure.kafka.support;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {
    
    private final List<ILoggingEvent> logEvents = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent event) {
        logEvents.add(event);
    }

    public List<ILoggingEvent> getLogEvents() {
        return new ArrayList<>(logEvents);
    }

    public List<String> getInfoMessages() {
        return logEvents.stream()
            .filter(event -> event.getLevel().toString().equals("INFO"))
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
    }

    public List<String> getErrorMessages() {
        return logEvents.stream()
            .filter(event -> event.getLevel().toString().equals("ERROR"))
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
    }

    public String getLastInfoMessage() {
        return getInfoMessages().stream()
            .reduce((first, second) -> second)
            .orElse("");
    }

    public String getLastErrorMessage() {
        return getErrorMessages().stream()
            .reduce((first, second) -> second)
            .orElse("");
    }

    public void clear() {
        logEvents.clear();
    }
}