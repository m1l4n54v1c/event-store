package io.event.thinking.eventstore.sample.api.event;

import java.io.Serializable;

public record CourseCapacityChanged(String id, int capacity) implements Serializable {

    public static final String NAME = "CourseCapacityChanged";
}