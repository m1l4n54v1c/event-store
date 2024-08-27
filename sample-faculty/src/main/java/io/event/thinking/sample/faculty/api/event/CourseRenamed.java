package io.event.thinking.sample.faculty.api.event;

public record CourseRenamed(String courseId, String newName) {

    public static final String NAME = "CourseRenamed";
}
