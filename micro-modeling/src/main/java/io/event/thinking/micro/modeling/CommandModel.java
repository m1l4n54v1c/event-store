package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.api.Criteria;

import java.util.List;

public interface CommandModel<T> {

    Criteria criteria(T command);

    void onEvent(Event event);

    List<Event> handle(T command);
}