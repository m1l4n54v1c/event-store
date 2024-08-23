package io.event.thinking.sample.faculty.api.event;

import java.io.Serializable;

public record CourseCapacityChanged(String id, int capacity) implements Serializable {

    public static final String NAME = "CourseCapacityChanged";
}