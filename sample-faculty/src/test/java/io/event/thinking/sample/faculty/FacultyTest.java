package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import io.event.thinking.micro.es.Serializer;
import io.event.thinking.micro.es.LocalCommandBus;
import io.event.thinking.sample.faculty.api.command.SubscribeStudent;
import io.event.thinking.sample.faculty.api.command.UnsubscribeStudent;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.model.SubscribeStudentCommandModel;
import io.event.thinking.sample.faculty.model.UnsubscribeStudentCommandModel;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.stream.IntStream;

import static io.event.thinking.eventstore.api.Event.event;
import static io.event.thinking.eventstore.api.Tag.tag;
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Constants.COURSE_ID;
import static io.event.thinking.sample.faculty.model.Constants.STUDENT_ID;

class FacultyTest {

    private final Serializer serializer = new Serializer() {
    };
    private EventStore eventStore;
    private LocalCommandBus commandBus;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        commandBus = new LocalCommandBus(eventStore);
        commandBus.register(SubscribeStudent.class, SubscribeStudentCommandModel::new);
        commandBus.register(UnsubscribeStudent.class, UnsubscribeStudentCommandModel::new);
    }

    @Test
    void successfulSubscription() {
        var studentId = enrollStudent();
        var courseId = createCourse();

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .expectNext(2L)
                    .verifyComplete();
    }

    @Test
    void studentAlreadySubscribed() {
        var studentId = enrollStudent();
        var courseId = createCourse();
        subscribe(studentId, courseId);

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student already subscribed to this course");
    }

    @Test
    void courseFullyBooked() {
        var student1Id = enrollStudent();
        var student2Id = enrollStudent();
        var student3Id = enrollStudent();
        var courseId = createCourse(2);
        subscribe(student1Id, courseId);
        subscribe(student2Id, courseId);

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(student3Id, courseId)))
                    .verifyErrorMessage("Course is fully booked");
    }

    @Test
    void studentSubscribedToTooManyCourses() {
        var studentId = enrollStudent();
        var targetCourseId = createCourse();
        IntStream.range(0, 10)
                 .mapToObj(i -> createCourse())
                 .forEach(courseId -> subscribe(studentId, courseId));

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, targetCourseId)))
                    .verifyErrorMessage("Student subscribed to too many courses");
    }

    @Test
    void studentDoesNotExist() {
        var studentId = UUID.randomUUID().toString();
        var courseId = createCourse();

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student with given id never enrolled the faculty");
    }

    @Test
    void courseDoesNotExist() {
        var studentId = enrollStudent();
        var courseId = UUID.randomUUID().toString();

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Course with given id does not exist");
    }

    private String enrollStudent() {
        var studentId = UUID.randomUUID().toString();
        eventStore.append(event(serializer.serialize(new StudentEnrolledFaculty(studentId, "Name", "Lastname")),
                                type(StudentEnrolledFaculty.NAME),
                                tag(STUDENT_ID, studentId)))
                  .block();
        return studentId;
    }

    private String createCourse() {
        return createCourse(10);
    }

    private String createCourse(int capacity) {
        var courseId = UUID.randomUUID().toString();
        eventStore.append(event(serializer.serialize(new CourseCreated(courseId, capacity)),
                                type(CourseCreated.NAME),
                                tag(COURSE_ID, courseId)))
                  .block();
        return courseId;
    }

    private void subscribe(String studentId, String courseId) {
        eventStore.append(event(serializer.serialize(new StudentSubscribed(studentId, courseId)),
                                type(StudentSubscribed.NAME),
                                tag(STUDENT_ID, studentId),
                                tag(COURSE_ID, courseId)))
                  .block();
    }
}
