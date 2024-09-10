package io.event.thinking.sample.faculty;

import io.event.thinking.micro.es.test.CommandHandlerFixture;
import io.event.thinking.sample.faculty.api.command.SubscribeStudent;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;
import io.event.thinking.sample.faculty.commandhandler.SubscribeStudentCommandHandler;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.event.thinking.sample.faculty.Indexing.multiEventIndexer;

class SubscribeStudentTest {

    private CommandHandlerFixture<SubscribeStudent> fixture;

    @BeforeEach
    void setUp() {
        fixture = new CommandHandlerFixture<>(SubscribeStudent.class,
                                              new SubscribeStudentCommandHandler(),
                                              multiEventIndexer());
    }

    @Test
    void successfulSubscription() {
        var studentId = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(studentId, "Stefan", "Dragisic"),
                      new CourseCreated(courseId, 10))
               .when(new SubscribeStudent(studentId, courseId))
               .expectEvents(new StudentSubscribed(studentId, courseId));
    }

    @Test
    void studentAlreadySubscribed() {
        var studentId = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(studentId, "Stefan", "Dragisic"),
                      new CourseCreated(courseId, 10),
                      new StudentSubscribed(studentId, courseId))
               .when(new SubscribeStudent(studentId, courseId))
               .expectException(RuntimeException.class, "Student already subscribed to this course");
    }

    @Test
    void courseFullyBooked() {
        var student1Id = UUID.randomUUID().toString();
        var student2Id = UUID.randomUUID().toString();
        var student3Id = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(student1Id, "Marc", "Gathier"),
                      new StudentEnrolledFaculty(student2Id, "Stefan", "Andjelkovic"),
                      new StudentEnrolledFaculty(student3Id, "Allard", "Buijze"),
                      new CourseCreated(courseId, 2),
                      new StudentSubscribed(student1Id, courseId),
                      new StudentSubscribed(student2Id, courseId))
               .when(new SubscribeStudent(student3Id, courseId))
               .expectException(RuntimeException.class, "Course is fully booked");
    }

    @Test
    void studentSubscribedToTooManyCourses() {
        var studentId = UUID.randomUUID().toString();
        var course1Id = UUID.randomUUID().toString();
        var course2Id = UUID.randomUUID().toString();
        var course3Id = UUID.randomUUID().toString();
        var course4Id = UUID.randomUUID().toString();
        var course5Id = UUID.randomUUID().toString();
        var course6Id = UUID.randomUUID().toString();
        var course7Id = UUID.randomUUID().toString();
        var course8Id = UUID.randomUUID().toString();
        var course9Id = UUID.randomUUID().toString();
        var course10Id = UUID.randomUUID().toString();
        var targetCourseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(studentId, "Milan", "Savic"),
                      new CourseCreated(targetCourseId, 10),
                      new CourseCreated(course1Id, 10),
                      new CourseCreated(course2Id, 10),
                      new CourseCreated(course3Id, 10),
                      new CourseCreated(course4Id, 10),
                      new CourseCreated(course5Id, 10),
                      new CourseCreated(course6Id, 10),
                      new CourseCreated(course7Id, 10),
                      new CourseCreated(course8Id, 10),
                      new CourseCreated(course9Id, 10),
                      new CourseCreated(course10Id, 10),
                      new StudentSubscribed(studentId, course1Id),
                      new StudentSubscribed(studentId, course2Id),
                      new StudentSubscribed(studentId, course3Id),
                      new StudentSubscribed(studentId, course4Id),
                      new StudentSubscribed(studentId, course5Id),
                      new StudentSubscribed(studentId, course6Id),
                      new StudentSubscribed(studentId, course7Id),
                      new StudentSubscribed(studentId, course8Id),
                      new StudentSubscribed(studentId, course9Id),
                      new StudentSubscribed(studentId, course10Id))
               .when(new SubscribeStudent(studentId, targetCourseId))
               .expectException(RuntimeException.class, "Student subscribed to too many courses");
    }

    @Test
    void studentDoesNotExist() {
        var studentId = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new CourseCreated(courseId, 10))
               .when(new SubscribeStudent(studentId, courseId))
               .expectException(RuntimeException.class, "Student with given id never enrolled the faculty");
    }

    @Test
    void courseDoesNotExist() {
        var studentId = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(studentId, "Stefan", "Mirkovic"))
               .when(new SubscribeStudent(studentId, courseId))
               .expectException(RuntimeException.class, "Course with given id does not exist");
    }

    @Test
    void subscribeAfterUnsubscription() {
        var studentId = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new CourseCreated(courseId, 10),
                      new StudentEnrolledFaculty(studentId, "Jovana", "Nogic"),
                      new StudentSubscribed(studentId, courseId),
                      new StudentUnsubscribed(studentId, courseId))
               .when(new SubscribeStudent(studentId, courseId))
               .expectEvents(new StudentSubscribed(studentId, courseId));
    }

    @Test
    void increaseCourseCapacityAllowsSubscribingToPreviouslyFullCourse() {
        var student1Id = UUID.randomUUID().toString();
        var student2Id = UUID.randomUUID().toString();
        var student3Id = UUID.randomUUID().toString();
        var courseId = UUID.randomUUID().toString();

        fixture.given(new StudentEnrolledFaculty(student1Id, "Bogdan", "Bogdanovic"),
                      new StudentEnrolledFaculty(student2Id, "Nikola", "Jokic"),
                      new StudentEnrolledFaculty(student3Id, "Vasilije", "Micic"),
                      new CourseCreated(courseId, 2),
                      new StudentSubscribed(student1Id, courseId),
                      new StudentSubscribed(student2Id, courseId),
                      new CourseCapacityChanged(courseId, 5))
               .when(new SubscribeStudent(student3Id, courseId))
               .expectEvents(new StudentSubscribed(student3Id, courseId));
    }
}