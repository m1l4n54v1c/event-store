package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import io.event.thinking.micro.es.LocalCommandBus;
import io.event.thinking.sample.faculty.api.command.UnsubscribeStudent;
import io.event.thinking.sample.faculty.model.UnsubscribeStudentCommandModel;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

class UnsubscribeStudentTest {

    private EventStore eventStore;
    private LocalCommandBus commandBus;

    private Fixtures fixtures;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        fixtures = new Fixtures(eventStore);
        commandBus = new LocalCommandBus(eventStore);
        commandBus.register(UnsubscribeStudent.class, UnsubscribeStudentCommandModel::new);
    }

    @Test
    void successfulUnsubscribe() {
        var studentId = fixtures.enrollStudent();
        var courseId = fixtures.createCourse();
        fixtures.subscribe(studentId, courseId);

        StepVerifier.create(commandBus.dispatch(new UnsubscribeStudent(studentId, courseId)))
                    .expectNext(3L)
                    .verifyComplete();
    }

    @Test
    void unsubscribeWithoutStudentBeingSubscribed() {
        var studentId = fixtures.enrollStudent();
        var courseId = fixtures.createCourse();

        StepVerifier.create(commandBus.dispatch(new UnsubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student is not subscribed to course");
    }

    @Test
    void unsubscribeAfterUnsubscription() {
        var studentId = fixtures.enrollStudent();
        var courseId = fixtures.createCourse();
        fixtures.subscribe(studentId, courseId);
        fixtures.unsubscribe(studentId, courseId);

        StepVerifier.create(commandBus.dispatch(new UnsubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student is not subscribed to course");
    }
}
