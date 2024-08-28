package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.api.Tag;

import static io.event.thinking.eventstore.api.Tag.tag;

public class Tags {

    public static final String STUDENT_ID = "studentId";
    public static final String COURSE_ID = "courseId";

    private Tags() {

    }

    public static Tag courseId(String courseId) {
        return tag(COURSE_ID, courseId);
    }

    public static Tag studentId(String studentId) {
        return tag(STUDENT_ID, studentId);
    }
}
