package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.Tag;
import io.event.thinking.micro.es.test.MultiEventTagger;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.CourseRenamed;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.Set;

import static io.event.thinking.micro.es.Tags.typeTag;
import static io.event.thinking.sample.faculty.commandhandler.FacultyTags.courseIdTag;
import static io.event.thinking.sample.faculty.commandhandler.FacultyTags.studentIdTag;

public class Tagging {

    public static MultiEventTagger multiEventTagger() {
        return new MultiEventTagger().register(StudentSubscribed.class, Tagging::tag)
                                     .register(StudentUnsubscribed.class, Tagging::tag)
                                     .register(CourseCapacityChanged.class, Tagging::tag)
                                     .register(CourseCreated.class, Tagging::tag)
                                     .register(StudentEnrolledFaculty.class, Tagging::tag)
                                     .register(CourseRenamed.class, Tagging::tag);
    }

    public static Set<Tag> tag(CourseRenamed payload) {
        return Set.of(typeTag(CourseRenamed.NAME),
                      courseIdTag(payload.courseId()));
    }

    public static Set<Tag> tag(StudentEnrolledFaculty payload) {
        return Set.of(typeTag(StudentEnrolledFaculty.NAME),
                      studentIdTag(payload.id()));
    }

    public static Set<Tag> tag(CourseCreated payload) {
        return Set.of(typeTag(CourseCreated.NAME),
                      courseIdTag(payload.id()));
    }

    public static Set<Tag> tag(CourseCapacityChanged payload) {
        return Set.of(typeTag(CourseCapacityChanged.NAME),
                      courseIdTag(payload.id()));
    }

    public static Set<Tag> tag(StudentUnsubscribed payload) {
        return Set.of(typeTag(StudentUnsubscribed.NAME),
                      courseIdTag(payload.courseId()),
                      studentIdTag(payload.studentId()));
    }

    public static Set<Tag> tag(StudentSubscribed payload) {
        return Set.of(typeTag(StudentSubscribed.NAME),
                      courseIdTag(payload.courseId()),
                      studentIdTag(payload.studentId()));
    }
}
