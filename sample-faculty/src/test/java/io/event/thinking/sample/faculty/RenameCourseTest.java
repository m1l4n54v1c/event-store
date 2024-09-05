package io.event.thinking.sample.faculty;

import io.event.thinking.micro.es.test.CommandModelFixture;
import io.event.thinking.sample.faculty.api.command.RenameCourse;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.CourseRenamed;
import io.event.thinking.sample.faculty.model.RenameCourseCommandModel;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.event.thinking.sample.faculty.Indexing.multiEventIndexer;

class RenameCourseTest {

    private CommandModelFixture<RenameCourse> fixture;

    @BeforeEach
    void setUp() {
        fixture = new CommandModelFixture<>(RenameCourse.class,
                                            new RenameCourseCommandModel(),
                                            multiEventIndexer());
    }

    @Test
    void successfulCourseRenaming() {
        var courseId = UUID.randomUUID().toString();

        fixture.given(new CourseCreated(courseId, "History", 10))
               .when(new RenameCourse(courseId, "History of Arts"))
               .expectEvents(new CourseRenamed(courseId, "History of Arts"));
    }

    @Test
    void renameCourseTwice() {
        var courseId = UUID.randomUUID().toString();

        fixture.given(new CourseCreated(courseId, "History", 42),
                      new CourseRenamed(courseId, "History of Arts"))
               .when(new RenameCourse(courseId, "Modern History of Arts"))
               .expectEvents(new CourseRenamed(courseId, "Modern History of Arts"));
    }

    @Test
    void renameCourseToTheSameName() {
        var courseId = UUID.randomUUID().toString();

        fixture.given(new CourseCreated(courseId, "History", 42))
               .when(new RenameCourse(courseId, "History"))
               .expectException(RuntimeException.class, "Course already has this name");
    }

    @Test
    void failsWhenRenamingCourseThatDoesNotExist() {
        var nonExistingId = UUID.randomUUID().toString();

        fixture.givenNoEvents()
               .when(new RenameCourse(nonExistingId, "History"))
               .expectException(RuntimeException.class, "Course with given id does not exist");
    }
}
