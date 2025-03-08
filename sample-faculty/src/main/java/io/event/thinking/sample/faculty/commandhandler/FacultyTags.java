package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Tag;

import static io.event.thinking.eventstore.api.Tag.tag;

public class FacultyTags {

    public static final String STUDENT_ID = "studentId";
    public static final String COURSE_ID = "courseId";

    private FacultyTags() {

    }

    public static Tag courseIdTag(String courseId) {
        return tag(COURSE_ID, courseId);
    }

    public static Tag studentIdTag(String studentId) {
        return tag(STUDENT_ID, studentId);
    }
}
