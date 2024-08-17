package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.EventStore;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.stream.IntStream;

class FacultyTest {

    private EventStore eventStore;
    private SubscriptionCommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        commandHandler = new SubscriptionCommandHandler(eventStore);
    }

    @Test
    void successfulSubscription() {
        var studentId = enrollStudent();
        var courseId = createCourse();

        StepVerifier.create(commandHandler.handle(new SubscribeStudent(studentId, courseId)))
                    .expectNext(2L)
                    .verifyComplete();
    }

    @Test
    void studentAlreadySubscribed() {
        var studentId = enrollStudent();
        var courseId = createCourse();
        subscribe(studentId, courseId);

        StepVerifier.create(commandHandler.handle(new SubscribeStudent(studentId, courseId)))
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

        StepVerifier.create(commandHandler.handle(new SubscribeStudent(student3Id, courseId)))
                    .verifyErrorMessage("Course is fully booked");
    }

    @Test
    void studentSubscribedToTooManyCourses() {
        var studentId = enrollStudent();
        var targetCourseId = createCourse();
        IntStream.range(0, 10)
                 .mapToObj(i -> createCourse())
                 .forEach(courseId -> subscribe(studentId, courseId));

        StepVerifier.create(commandHandler.handle(new SubscribeStudent(studentId, targetCourseId)))
                    .verifyErrorMessage("Student subscribed to too many courses");
    }

    @Test
    void studentDoesNotExist() {
        var studentId = UUID.randomUUID().toString();
        var courseId = createCourse();

        StepVerifier.create(commandHandler.handle(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Student with given id never enrolled the faculty");
    }

    @Test
    void courseDoesNotExist() {
        var studentId = enrollStudent();
        var courseId = UUID.randomUUID().toString();

        StepVerifier.create(commandHandler.handle(new SubscribeStudent(studentId, courseId)))
                    .verifyErrorMessage("Course with given id does not exist");
    }

    private String enrollStudent() {
        var studentId = UUID.randomUUID().toString();
        eventStore.append(new StudentEnrolled(studentId, "Name", "Lastname").toEvent())
                  .block();
        return studentId;
    }

    private String createCourse() {
        return createCourse(10);
    }

    private String createCourse(int capacity) {
        var courseId = UUID.randomUUID().toString();
        eventStore.append(new CourseCreated(courseId, capacity).toEvent())
                  .block();
        return courseId;
    }

    private void subscribe(String studentId, String courseId) {
        eventStore.append(new StudentSubscribed(studentId, courseId).toEvent())
                  .block();
    }
}
