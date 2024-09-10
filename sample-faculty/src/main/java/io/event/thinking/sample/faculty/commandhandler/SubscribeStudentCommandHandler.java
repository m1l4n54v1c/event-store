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

import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.courseIdIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.studentIdIndex;

public class SubscribeStudentCommandHandler
        implements DcbCommandHandler<SubscribeStudent, SubscribeStudentCommandHandler.State> {

    @Override
    public Criteria criteria(SubscribeStudent cmd) {
        return Criteria.criteria(criterion(typeIndex(StudentEnrolledFaculty.NAME), studentIdIndex(cmd.studentId())),
                                 criterion(typeIndex(CourseCreated.NAME), courseIdIndex(cmd.courseId())),
                                 criterion(typeIndex(CourseCapacityChanged.NAME), courseIdIndex(cmd.courseId())),
                                 criterion(typeIndex(StudentSubscribed.NAME), courseIdIndex(cmd.courseId())),
                                 criterion(typeIndex(StudentSubscribed.NAME), studentIdIndex(cmd.studentId())),
                                 criterion(typeIndex(StudentUnsubscribed.NAME), courseIdIndex(cmd.courseId())),
                                 criterion(typeIndex(StudentUnsubscribed.NAME), studentIdIndex(cmd.studentId())));
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
                             typeIndex(StudentSubscribed.NAME),
                             studentIdIndex(payload.studentId()),
                             courseIdIndex(payload.courseId())));
    }

    @Override
    public State source(Object event, State state) {
        return switch (event) {
            case StudentEnrolledFaculty e -> state.withStudentEnrolled(e);
            case CourseCreated e -> state.withCourseCreated(e);
            case CourseCapacityChanged e -> state.withCourseCapacityChanged(e);
            case StudentSubscribed e -> state.withStudentSubscribed(e);
            case StudentUnsubscribed e -> state.withStudentUnsubscribed(e);
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

        public State withCourseCreated(CourseCreated evt) {
            return new State(studentId, evt.id(), noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, evt.capacity(), alreadySubscribed);
        }

        public State withStudentEnrolled(StudentEnrolledFaculty evt) {
            return new State(evt.id(), courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
        }

        public State withStudentSubscribed(StudentSubscribed evt) {
            if (evt.studentId().equals(studentId) && evt.courseId().equals(courseId)) {
                return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, true);
            } else if (evt.studentId().equals(studentId)) {
                return new State(studentId, courseId, noOfCoursesStudentSubscribed + 1, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
            } else {
                return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse + 1, courseCapacity, alreadySubscribed);
            }
        }

        public State withStudentUnsubscribed(StudentUnsubscribed evt) {
            if (evt.studentId().equals(studentId) && evt.courseId().equals(courseId)) {
                return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, courseCapacity, false);
            } else if (evt.studentId().equals(studentId)) {
                return new State(studentId, courseId, noOfCoursesStudentSubscribed - 1, noOfStudentsSubscribedToCourse, courseCapacity, alreadySubscribed);
            } else {
                return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse - 1, courseCapacity, alreadySubscribed);
            }
        }

        public State withCourseCapacityChanged(CourseCapacityChanged evt) {
            return new State(studentId, courseId, noOfCoursesStudentSubscribed, noOfStudentsSubscribedToCourse, evt.capacity(), alreadySubscribed);
        }

        public void assertStudentEnrolledFaculty() {
            if (studentId == null) {
                throw new RuntimeException("Student with given id never enrolled the faculty");
            }
        }

        private void assertStudentNotSubscribedToTooManyCourses() {
            if (noOfCoursesStudentSubscribed == MAX_COURSES_PER_STUDENT) {
                throw new RuntimeException("Student subscribed to too many courses");
            }
        }

        private void assertEnoughVacantSpotsInCourse() {
            if (noOfStudentsSubscribedToCourse == courseCapacity) {
                throw new RuntimeException("Course is fully booked");
            }
        }

        private void assertStudentNotAlreadySubscribed() {
            if (alreadySubscribed) {
                throw new RuntimeException("Student already subscribed to this course");
            }
        }

        private void assertCourseExists() {
            if (courseId == null) {
                throw new RuntimeException("Course with given id does not exist");
            }
        }
    }
}