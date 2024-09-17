package io.event.thinking.sample.faculty;

import io.event.thinking.micro.es.test.CommandHandlerFixture;
import io.event.thinking.sample.faculty.api.command.ChangeCourseCapacity;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;
import io.event.thinking.sample.faculty.commandhandler.ChangeCourseCapacityCommandHandler;
import io.event.thinking.sample.faculty.commandhandler.FacultyIndices;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.Indexing.multiEventIndexer;

class ChangeCourseCapacityTest {

    private CommandHandlerFixture<ChangeCourseCapacity> fixture;

    @BeforeEach
    void setUp() {
        fixture = new CommandHandlerFixture<>(ChangeCourseCapacity.class,
                                              new ChangeCourseCapacityCommandHandler(),
                                              multiEventIndexer());
    }

    @Test
    void unsubscribeStudentsFromCourseIfCapacityIsReduced() {
        var student1Id = UUID.randomUUID().toString();
        var student2Id = UUID.randomUUID().toString();
        var student3Id = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(student1Id, "Milan", "Savic"),
                      new StudentEnrolledFaculty(student2Id, "Marco", "Amman"),
                      new StudentEnrolledFaculty(student3Id, "Ivan", "Dugalic"),
                      new CourseCreated(courseId, "Math", 42),
                      new StudentSubscribed(student1Id, courseId),
                      new StudentSubscribed(student2Id, courseId),
                      new StudentSubscribed(student3Id, courseId))
               .when(new ChangeCourseCapacity(courseId, 1))
               .expectEvents(new CourseCapacityChanged(courseId, 1),
                             new StudentUnsubscribed(student2Id, courseId),
                             new StudentUnsubscribed(student3Id, courseId));
    }

    @Test
    void courseCapacityChangeFailsOnNonExistingCourse() {
        fixture.givenNoEvents()
               .when(new ChangeCourseCapacity(UUID.randomUUID().toString(), 1))
               .expectException(RuntimeException.class, "Course with given id does not exist");
    }

    @Test
    void courseCapacityChangeFailsOnNegativeCapacity() {
        var courseId = UUID.randomUUID().toString();

        CourseCreated courseCreated = new CourseCreated(courseId, "Math", 42);
        fixture.given(event(courseCreated,
                            typeIndex(CourseCreated.NAME),
                            FacultyIndices.courseIdIndex(courseCreated.id())))
               .when(new ChangeCourseCapacity(courseId, -1))
               .expectException(RuntimeException.class, "Course capacity cannot be negative");
    }
}