package com.example.budget_management_app.common.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public void register(Object event) {
        publisher.publishEvent(event);
    }
}
