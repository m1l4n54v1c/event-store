package io.event.thinking.sample.faculty;

import io.event.thinking.micro.es.test.CommandHandlerFixture;
import io.event.thinking.sample.faculty.api.command.UnsubscribeStudent;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;
import io.event.thinking.sample.faculty.commandhandler.UnsubscribeStudentCommandHandler;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.event.thinking.sample.faculty.Tagging.multiEventTagger;

class UnsubscribeStudentTest {

    private CommandHandlerFixture<UnsubscribeStudent> fixture;

    @BeforeEach
    void setUp() {
        fixture = new CommandHandlerFixture<>(UnsubscribeStudent.class,
                                              new UnsubscribeStudentCommandHandler(),
                                              multiEventTagger());
    }

    @Test
    void successfulUnsubscribe() {
        var studentId = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(studentId, "Novak", "Djokovic"),
                      new CourseCreated(courseId, "Tennis", 1),
                      new StudentSubscribed(studentId, courseId))
               .when(new UnsubscribeStudent(studentId, courseId))
               .expectEvents(new StudentUnsubscribed(studentId, courseId));
    }

    @Test
    void unsubscribeWithoutStudentBeingSubscribed() {
        var courseId = UUID.randomUUID().toString();
        var studentId = UUID.randomUUID().toString();

        fixture.givenNoEvents()
               .when(new UnsubscribeStudent(studentId, courseId))
               .expectException(RuntimeException.class, "Student is not subscribed to course");
    }

    @Test
    void unsubscribeAfterUnsubscription() {
        var courseId = UUID.randomUUID().toString();
        var studentId = UUID.randomUUID().toString();

        fixture.given(new StudentSubscribed(studentId, courseId),
                      new StudentUnsubscribed(studentId, courseId))
               .when(new UnsubscribeStudent(studentId, courseId))
               .expectException(RuntimeException.class, "Student is not subscribed to course");
    }
}
