package io.event.thinking.sample.faculty.api.event;

import java.io.Serializable;

public record CourseCreated(String id, String name, int capacity) implements Serializable {

    public static final String NAME = "CourseCreated";
}
