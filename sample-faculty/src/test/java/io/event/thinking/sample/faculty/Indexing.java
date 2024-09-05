package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.Index;
import io.event.thinking.micro.es.test.MultiEventIndexer;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.Set;

import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.model.Indices.courseIdIndex;
import static io.event.thinking.sample.faculty.model.Indices.studentIdIndex;

public class Indexing {

    public static MultiEventIndexer multiEventIndexer() {
        return new MultiEventIndexer().register(StudentSubscribed.class, Indexing::index)
                                      .register(StudentUnsubscribed.class, Indexing::index)
                                      .register(CourseCapacityChanged.class, Indexing::index)
                                      .register(CourseCreated.class, Indexing::index)
                                      .register(StudentEnrolledFaculty.class, Indexing::index);
    }

    public static Set<Index> index(StudentEnrolledFaculty payload) {
        return Set.of(typeIndex(StudentEnrolledFaculty.NAME),
                      studentIdIndex(payload.id()));
    }

    public static Set<Index> index(CourseCreated payload) {
        return Set.of(typeIndex(CourseCreated.NAME),
                      courseIdIndex(payload.id()));
    }

    public static Set<Index> index(CourseCapacityChanged payload) {
        return Set.of(typeIndex(CourseCapacityChanged.NAME),
                      courseIdIndex(payload.id()));
    }

    public static Set<Index> index(StudentUnsubscribed payload) {
        return Set.of(typeIndex(StudentUnsubscribed.NAME),
                      courseIdIndex(payload.courseId()),
                      studentIdIndex(payload.studentId()));
    }

    public static Set<Index> index(StudentSubscribed payload) {
        return Set.of(typeIndex(StudentSubscribed.NAME),
                      courseIdIndex(payload.courseId()),
                      studentIdIndex(payload.studentId()));
    }
}
