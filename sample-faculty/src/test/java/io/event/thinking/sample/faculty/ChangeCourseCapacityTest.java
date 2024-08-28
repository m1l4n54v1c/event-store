package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.api.SequencedEvent;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import io.event.thinking.micro.es.LocalCommandBus;
import io.event.thinking.micro.es.Serializer;
import io.event.thinking.sample.faculty.api.command.ChangeCourseCapacity;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;
import io.event.thinking.sample.faculty.model.ChangeCourseCapacityCommandModel;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static io.event.thinking.eventstore.api.Criteria.criteria;
import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Tags.courseIdTag;

class ChangeCourseCapacityTest {

    private final Serializer serializer = new Serializer() {
    };
    private EventStore eventStore;
    private LocalCommandBus commandBus;

    private Fixtures fixtures;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        fixtures = new Fixtures(eventStore, serializer);
        commandBus = new LocalCommandBus(eventStore);
        commandBus.register(ChangeCourseCapacity.class, ChangeCourseCapacityCommandModel::new);
    }

    @Test
    void unsubscribeStudentsFromCourseIfCapacityIsReduced() {
        var student1 = fixtures.enrollStudent();
        var student2 = fixtures.enrollStudent();
        var student3 = fixtures.enrollStudent();
        var courseId = fixtures.createCourse(42);

        List.of(student1, student2, student3)
            .forEach(studentId -> fixtures.subscribe(studentId, courseId));

        StepVerifier.create(commandBus.dispatch(new ChangeCourseCapacity(courseId, 1)))
                    .expectNextCount(1)
                    .verifyComplete();

        var unsubscribedStudents = eventStore.read(criteria(criterion(type(StudentUnsubscribed.NAME),
                                                                      courseIdTag(courseId))))
                                             .flux()
                                             .map(SequencedEvent::event)
                                             .map(e -> (StudentUnsubscribed) serializer.deserialize(e.payload()))
                                             .map(StudentUnsubscribed::studentId);
        StepVerifier.create(unsubscribedStudents)
                    .expectNext(student2, student3)
                    .verifyComplete();
    }

    @Test
    void courseCapacityChangeFailsOnNonExistingCourse() {
        StepVerifier.create(commandBus.dispatch(new ChangeCourseCapacity(UUID.randomUUID().toString(), 1)))
                    .verifyError();
    }

    @Test
    void courseCapacityChangeFailsOnNegativeCapacity() {
        var courseId = fixtures.createCourse(42);
        StepVerifier.create(commandBus.dispatch(new ChangeCourseCapacity(courseId, -10)))
                    .verifyError();
    }
}
