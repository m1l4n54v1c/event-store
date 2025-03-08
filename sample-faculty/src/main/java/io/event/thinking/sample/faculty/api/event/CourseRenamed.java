package io.event.thinking.sample.faculty.api.event;

import java.io.Serializable;

public record CourseRenamed(String courseId, String newName) implements Serializable {

    public static final String NAME = "CourseRenamed";
}
