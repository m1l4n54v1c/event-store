package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.SubscribeStudent;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.List;

import static io.event.thinking.eventstore.api.Criteria.anyOf;
import static io.event.thinking.eventstore.api.Criterion.allOf;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Tags.typeTag;
import static io.event.thinking.sample.faculty.commandhandler.FacultyTags.courseIdTag;
import static io.event.thinking.sample.faculty.commandhandler.FacultyTags.studentIdTag;

public class SubscribeStudentCommandHandler
        implements DcbCommandHandler<SubscribeStudent, SubscribeStudentCommandHandler.State> {

    /*
        We need all events, that show
        - this student has been enrolled
        - this course has been created
        - this course capacity was changed
        - all subscriptions or unsubscriptions that concern either this course or student
     */
    @Override
    public Criteria criteria(SubscribeStudent cmd) {
        return anyOf(
                // this student has enrolled
                allOf(typeTag(StudentEnrolledFaculty.NAME), studentIdTag(cmd.studentId())),
                // this course has been created
                allOf(typeTag(CourseCreated.NAME), courseIdTag(cmd.courseId())),
                // the capacity of this course has been changed
                allOf(typeTag(CourseCapacityChanged.NAME), courseIdTag(cmd.courseId())),
                // all students subscribed to this course
                allOf(typeTag(StudentSubscribed.NAME), courseIdTag(cmd.courseId())),
                // all courses this student subscribed to
                allOf(typeTag(StudentSubscribed.NAME), studentIdTag(cmd.studentId())),
                // all students unsubscribed from this course
                allOf(typeTag(StudentUnsubscribed.NAME), courseIdTag(cmd.courseId())),
                // all courses this student unsubscribed from
                allOf(typeTag(StudentUnsubscribed.NAME), studentIdTag(cmd.studentId())));
    }

    @Override
    public State initialState() {
        return State.initial();
    }

    @Override
    public List<Event> handle(SubscribeStudent cmd, State state) {
        state.assertStudentEnrolledFaculty();
        state.assertCourseExists();
        state.assertStudentNotAlreadySubscribed();
        state.assertEnoughVacantSpotsInCourse();
        state.assertStudentNotSubscribedToTooManyCourses();
        StudentSubscribed payload = new StudentSubscribed(cmd.studentId(), cmd.courseId());
        return List.of(event(payload,
                             typeTag(StudentSubscribed.NAME),
                             studentIdTag(payload.studentId()),
                             courseIdTag(payload.courseId())));
    }

    @Override
    public State source(Object event, State state) {
        return switch (event) {
            case StudentEnrolledFaculty e -> state.withStudentId(e.id());
            case CourseCreated e -> state.withCourseId(e.id())
                                         .withCourseCapacity(e.capacity());
            case CourseCapacityChanged e -> state.withCourseCapacity(e.capacity());
            case StudentSubscribed e -> state.withStudentSubscribed(e.studentId(), e.courseId());
            case StudentUnsubscribed e -> state.withStudentUnsubscribed(e.studentId(), e.courseId());
            default -> throw new RuntimeException("No handler for this event");
        };
    }

    public record State(String studentId,
                        String courseId,
                        int noOfCoursesStudentSubscribed,
                        int noOfStudentsSubscribedToCourse,
                        int courseCapacity,
                        boolean alreadySubscribed) {

        private static final int MAX_COURSES_PER_STUDENT = 10;

        public static State initial() {
            return new State(null, null, 0, 0, 0, false);
        }

        public State withStudentId(String studentId) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withCourseId(String courseId) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withNoOfCoursesStudentSubscribed(int noOfCoursesStudentSubscribed) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withNoOfStudentsSubscribedToCourse(int noOfStudentsSubscribedToCourse) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withCourseCapacity(int courseCapacity) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withAlreadySubscribed(boolean alreadySubscribed) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withStudentSubscribed(String enrolledStudentId, String enrolledCourseId) {
            if (enrolledStudentId.equals(studentId) && enrolledCourseId.equals(courseId)) {
                return withAlreadySubscribed(true);
            } else if (enrolledStudentId.equals(studentId)) {
                return withNoOfCoursesStudentSubscribed(noOfCoursesStudentSubscribed + 1);
            } else {
                return withNoOfStudentsSubscribedToCourse(noOfStudentsSubscribedToCourse + 1);
            }
        }

        public State withStudentUnsubscribed(String enrolledStudentId, String enrolledCourseId) {
            if (enrolledStudentId.equals(studentId) && enrolledCourseId.equals(courseId)) {
                return withAlreadySubscribed(false);
            } else if (enrolledStudentId.equals(studentId)) {
                return withNoOfCoursesStudentSubscribed(noOfCoursesStudentSubscribed - 1);
            } else {
                return withNoOfStudentsSubscribedToCourse(noOfStudentsSubscribedToCourse - 1);
            }
        }

        public void assertStudentEnrolledFaculty() {
            if (studentId == null) {
                throw new RuntimeException("Student with given id never enrolled the faculty");
            }
        }

        public void assertStudentNotSubscribedToTooManyCourses() {
            if (noOfCoursesStudentSubscribed == MAX_COURSES_PER_STUDENT) {
                throw new RuntimeException("Student subscribed to too many courses");
            }
        }

        public void assertEnoughVacantSpotsInCourse() {
            if (noOfStudentsSubscribedToCourse == courseCapacity) {
                throw new RuntimeException("Course is fully booked");
            }
        }

        public void assertStudentNotAlreadySubscribed() {
            if (alreadySubscribed) {
                throw new RuntimeException("Student already subscribed to this course");
            }
        }

        public void assertCourseExists() {
            if (courseId == null) {
                throw new RuntimeException("Course with given id does not exist");
            }
        }
    }
}