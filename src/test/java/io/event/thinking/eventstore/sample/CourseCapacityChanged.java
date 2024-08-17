package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.Event;

import static io.event.thinking.eventstore.Event.event;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.sample.Constants.COURSE_ID;
import static io.event.thinking.eventstore.sample.Constants.EVENT_TYPE;

record CourseCapacityChanged(String id, int capacity) implements SerializableEvent {

    static final String NAME = "CourseCapacityChanged";

    @Override
    public Event toEvent() {
        return event(serialize(), tag(EVENT_TYPE, NAME), tag(COURSE_ID, id));
    }

    static CourseCapacityChanged from(byte[] bytes) {
        return SerializableEvent.deserialize(bytes);
    }
}
