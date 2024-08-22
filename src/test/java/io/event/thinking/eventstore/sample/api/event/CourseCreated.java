package io.event.thinking.eventstore.sample.api.event;

import java.io.Serializable;

public record CourseCreated(String id, int capacity) implements Serializable {

    public static final String NAME = "CourseCreated";
}
