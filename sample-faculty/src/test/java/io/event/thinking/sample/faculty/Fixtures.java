package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.Event;
import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.micro.es.Serializer;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.CourseRenamed;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.UUID;

import static io.event.thinking.eventstore.api.Event.event;
import static io.event.thinking.eventstore.api.Tag.tag;
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Constants.COURSE_ID;
import static io.event.thinking.sample.faculty.model.Constants.STUDENT_ID;

public class Fixtures {

    private final Serializer serializer;
    private final EventStore eventStore;

    public Fixtures(EventStore eventStore) {
        this(eventStore, new Serializer() {
        });
    }

    public Fixtures(EventStore eventStore, Serializer serializer) {
        this.eventStore = eventStore;
        this.serializer = serializer;
    }

    String enrollStudent() {
        var studentId = UUID.randomUUID().toString();
        eventStore.append(event(serializer.serialize(new StudentEnrolledFaculty(studentId, "Name", "Lastname")),
                                type(StudentEnrolledFaculty.NAME),
                                tag(STUDENT_ID, studentId)))
                  .block();
        return studentId;
    }

    String createCourse() {
        return createCourse(10);
    }

    String createCourse(String name) {
        return createCourse(10, name);
    }

    String createCourse(int capacity) {
        return createCourse(capacity, "Maths");
    }

    String createCourse(int capacity, String name) {
        var courseId = UUID.randomUUID().toString();
        eventStore.append(event(serializer.serialize(new CourseCreated(courseId, name, capacity)),
                                type(CourseCreated.NAME),
                                tag(COURSE_ID, courseId)))
                  .block();
        return courseId;
    }

    void subscribe(String studentId, String courseId) {
        eventStore.append(event(serializer.serialize(new StudentSubscribed(studentId, courseId)),
                                type(StudentSubscribed.NAME),
                                tag(STUDENT_ID, studentId),
                                tag(COURSE_ID, courseId)))
                  .block();
    }

    void unsubscribe(String studentId, String courseId) {
        eventStore.append(event(serializer.serialize(new StudentUnsubscribed(studentId, courseId)),
                                type(StudentUnsubscribed.NAME),
                                tag(STUDENT_ID, studentId),
                                tag(COURSE_ID, courseId)))
                  .block();
    }

    void renameCourse(String courseId, String newCourseName) {
        eventStore.append(courseRenamedEvent(courseId, newCourseName))
                  .block();
    }

    Event courseRenamedEvent(String courseId, String newCourseName) {
        return event(serializer.serialize(new CourseRenamed(courseId, newCourseName)),
                     type(CourseRenamed.NAME),
                     tag(COURSE_ID, courseId));
    }

    void changeCourseCapacity(String courseId, int newCapacity){
        eventStore.append(courseCapacityChangedEvent(courseId, newCapacity))
                  .block();
    }

    Event courseCapacityChangedEvent(String courseId, int newCapacity) {
        return event(serializer.serialize(new CourseCapacityChanged(courseId, newCapacity)),
                     type(CourseCapacityChanged.NAME),
                     tag(COURSE_ID, courseId));
    }
}
