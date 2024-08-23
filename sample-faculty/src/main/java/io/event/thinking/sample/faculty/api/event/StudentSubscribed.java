package io.event.thinking.sample.faculty.api.event;

import java.io.Serializable;

public record StudentSubscribed(String studentId, String courseId) implements Serializable {

    public static final String NAME = "StudentSubscribed";
}
