package io.event.thinking.sample.faculty;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import io.event.thinking.micro.es.LocalCommandBus;
import io.event.thinking.micro.es.Serializer;
import io.event.thinking.sample.faculty.api.command.RenameCourse;
import io.event.thinking.sample.faculty.api.event.CourseRenamed;
import io.event.thinking.sample.faculty.model.RenameCourseCommandModel;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.util.UUID;

import static io.event.thinking.eventstore.api.Criteria.criteria;
import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Tags.courseId;

class RenameStudentTest {

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
        commandBus.register(RenameCourse.class, RenameCourseCommandModel::new);
    }

    @Test
    void successfulCourseRenaming() {
        var courseId = fixtures.createCourse("History");

        String newName = "History of Arts";
        StepVerifier.create(commandBus.dispatch(new RenameCourse(courseId, newName)))
                    .expectNext(1L)
                    .verifyComplete();

        var courseRenamed = event(new CourseRenamed(courseId, newName),
                                  type(CourseRenamed.NAME),
                                  courseId(courseId));
        StepVerifier.create(eventStore.read(criteria(criterion(type(CourseRenamed.NAME), courseId(courseId))))
                                      .flux()
                                      .map(s -> event(s.event().tags(), serializer.deserialize(s.event().payload()))))
                    .expectNext(courseRenamed)
                    .verifyComplete();
    }

    @Test
    void renameCourseTwice() {
        var courseId = fixtures.createCourse("History");
        fixtures.renameCourse(courseId, "History of Arts");

        String newName = "Modern History of Arts";
        StepVerifier.create(commandBus.dispatch(new RenameCourse(courseId, newName)))
                    .expectNext(2L)
                    .verifyComplete();

        var courseRenamed = event(new CourseRenamed(courseId, newName),
                                  type(CourseRenamed.NAME),
                                  courseId(courseId));
        StepVerifier.create(eventStore.read(criteria(criterion(type(CourseRenamed.NAME), courseId(courseId))))
                                      .flux()
                                      .last()
                                      .map(s -> event(s.event().tags(), serializer.deserialize(s.event().payload()))))
                    .expectNext(courseRenamed)
                    .verifyComplete();
    }

    @Test
    void failsWhenRenamingCourseThatDoesNotExist() {
        String nonExistingId = UUID.randomUUID().toString();
        StepVerifier.create(commandBus.dispatch(new RenameCourse(nonExistingId, "newName")))
                    .verifyError();
    }
}