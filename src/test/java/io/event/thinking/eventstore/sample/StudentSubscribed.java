package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.Event;

import static io.event.thinking.eventstore.Event.event;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.sample.Constants.*;

record StudentSubscribed(String studentId, String courseId) implements SerializableEvent {

    static final String NAME = "StudentSubscribed";

    @Override
    public Event toEvent() {
        return event(serialize(), tag(EVENT_TYPE, NAME), tag(STUDENT_ID, studentId), tag(COURSE_ID, courseId));
    }

    static StudentSubscribed from(byte[] bytes) {
        return SerializableEvent.deserialize(bytes);
    }
}
