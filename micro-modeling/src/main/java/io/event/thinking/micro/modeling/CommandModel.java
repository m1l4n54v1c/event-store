package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.Criteria;
import reactor.core.publisher.Flux;

import java.util.List;

public interface CommandModel<T> {

    Criteria buildCriteria(T command);

    void onEvent(Event event);

    List<Event> handle(T command);
}