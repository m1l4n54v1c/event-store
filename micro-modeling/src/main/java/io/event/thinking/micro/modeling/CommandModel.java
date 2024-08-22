package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.Criteria;
import reactor.core.publisher.Flux;

public interface CommandModel<T> {

    Criteria buildCriteria(T command);

    void onEvent(Event event);
//        String eventType = event.tags()
//                                .stream()
//                                .filter(t -> EVENT_TYPE.equals(t.key()))
//                                .map(Tag::value)
//                                .findFirst()
//                                .orElseThrow(() -> new RuntimeException("An event without type"));
//        onEvent(eventType, event.payload());

    Flux<Event> handle(T command);
}