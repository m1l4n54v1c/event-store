package io.event.thinking.sample.faculty.api.event;

import java.io.Serializable;

public record StudentEnrolledFaculty(String id, String firstName, String lastName) implements Serializable {

    public static final String NAME = "StudentEnrolled";
}
