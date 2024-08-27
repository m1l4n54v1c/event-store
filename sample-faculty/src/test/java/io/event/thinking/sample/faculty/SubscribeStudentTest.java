package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import io.event.thinking.micro.es.LocalCommandBus;
import io.event.thinking.sample.faculty.api.command.SubscribeStudent;
import io.event.thinking.sample.faculty.model.SubscribeStudentCommandModel;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.stream.IntStream;

class SubscribeStudentTest {

    private EventStore eventStore;
    private LocalCommandBus commandBus;

    private Fixtures fixtures;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        fixtures = new Fixtures(eventStore);
        commandBus = new LocalCommandBus(eventStore);
        commandBus.register(SubscribeStudent.class, SubscribeStudentCommandModel::new);
    }

    @Test
    void successfulSubscription() {
        var studentId = fixtures.enrollStudent();
        var courseId = fixtures.createCourse();

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .expectNext(2L)
                    .verifyComplete();
    }

    @Test
    void studentAlreadySubscribed() {
        var studentId = fixtures.enrollStudent();
        var courseId = fixtures.createCourse();
        fixtures.subscribe(studentId, courseId);

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student already subscribed to this course");
    }

    @Test
    void courseFullyBooked() {
        var student1Id = fixtures.enrollStudent();
        var student2Id = fixtures.enrollStudent();
        var student3Id = fixtures.enrollStudent();
        var courseId = fixtures.createCourse(2);
        fixtures.subscribe(student1Id, courseId);
        fixtures.subscribe(student2Id, courseId);

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(student3Id, courseId)))
                    .verifyErrorMessage("Course is fully booked");
    }

    @Test
    void studentSubscribedToTooManyCourses() {
        var studentId = fixtures.enrollStudent();
        var targetCourseId = fixtures.createCourse();
        IntStream.range(0, 10)
                 .mapToObj(i -> fixtures.createCourse())
                 .forEach(courseId -> fixtures.subscribe(studentId, courseId));

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, targetCourseId)))
                    .verifyErrorMessage("Student subscribed to too many courses");
    }

    @Test
    void studentDoesNotExist() {
        var studentId = UUID.randomUUID().toString();
        var courseId = fixtures.createCourse();

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student with given id never enrolled the faculty");
    }

    @Test
    void courseDoesNotExist() {
        var studentId = fixtures.enrollStudent();
        var courseId = UUID.randomUUID().toString();

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Course with given id does not exist");
    }

    @Test
    void subscribeAfterUnsubscription() {
        var studentId = fixtures.enrollStudent();
        var courseId = fixtures.createCourse();
        fixtures.subscribe(studentId, courseId);
        fixtures.unsubscribe(studentId, courseId);

        StepVerifier.create(commandBus.dispatch(new SubscribeStudent(studentId, courseId)))
                    .expectNext(4L)
                    .verifyComplete();
    }
}
