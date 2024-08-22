package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.Criteria;
import io.event.thinking.micro.modeling.CommandModel;
import io.event.thinking.micro.modeling.Event;
import io.event.thinking.sample.faculty.api.command.SubscribeStudent;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentEnrolledFaculty;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;
import reactor.core.publisher.Flux;

import static io.event.thinking.eventstore.Criteria.criteria;
import static io.event.thinking.eventstore.Criterion.criterion;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.micro.modeling.Event.event;
import static io.event.thinking.micro.modeling.TagUtils.type;
import static io.event.thinking.sample.faculty.model.Constants.COURSE_ID;
import static io.event.thinking.sample.faculty.model.Constants.STUDENT_ID;

public class SubscribeStudentCommandModel implements CommandModel<SubscribeStudent> {

    private static final int MAX_COURSES_PER_STUDENT = 10;

    private String studentId;
    private String courseId;
    private int noOfCoursesStudentSubscribed;
    private int noOfStudentsSubscribedToCourse;
    private int courseCapacity;
    private boolean alreadySubscribed;

    @Override
    public Flux<Event> handle(SubscribeStudent cmd) {
        if (studentId == null) {
            return Flux.error(new RuntimeException("Student with given id never enrolled the faculty"));
        }
        if (courseId == null) {
            return Flux.error(new RuntimeException("Course with given id does not exist"));
        }
        if (alreadySubscribed) {
            return Flux.error(new RuntimeException("Student already subscribed to this course"));
        }
        if (noOfStudentsSubscribedToCourse == courseCapacity) {
            return Flux.error(new RuntimeException("Course is fully booked"));
        }
        if (noOfCoursesStudentSubscribed == MAX_COURSES_PER_STUDENT) {
            return Flux.error(new RuntimeException("Student subscribed to too many courses"));
        }
        return Flux.just(tagEvent(new StudentSubscribed(studentId, courseId)));
    }

    @Override
    public Criteria buildCriteria(SubscribeStudent cmd) {
        return criteria(criterion(type(StudentEnrolledFaculty.NAME), tag(STUDENT_ID, cmd.studentId())),
                        criterion(type(CourseCreated.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(type(CourseCapacityChanged.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(type(StudentSubscribed.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(type(StudentSubscribed.NAME), tag(STUDENT_ID, cmd.studentId())),
                        criterion(type(StudentUnsubscribed.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(type(StudentUnsubscribed.NAME), tag(STUDENT_ID, cmd.studentId())));
    }


    void on(CourseCreated evt) {
        this.courseId = evt.id();
        this.courseCapacity = evt.capacity();
    }

    void on(StudentEnrolledFaculty evt) {
        this.studentId = evt.id();
    }

    void on(CourseCapacityChanged evt) {
        this.courseCapacity = evt.capacity();
    }

    void on(StudentSubscribed evt) {
        if (evt.studentId().equals(studentId) && evt.courseId().equals(courseId)) {
            alreadySubscribed = true;
        } else if (evt.studentId().equals(studentId)) {
            noOfCoursesStudentSubscribed++;
        } else {
            noOfStudentsSubscribedToCourse++;
        }
    }

    void on(StudentUnsubscribed evt) {
        if (evt.studentId().equals(studentId) && evt.courseId().equals(courseId)) {
            alreadySubscribed = false;
        } else if (evt.studentId().equals(studentId)) {
            noOfCoursesStudentSubscribed--;
        } else {
            noOfStudentsSubscribedToCourse--;
        }
    }

    private static Event tagEvent(StudentSubscribed event) {
        return event(event,
                     type(StudentSubscribed.NAME),
                     tag(STUDENT_ID, event.studentId()),
                     tag(COURSE_ID, event.courseId()));
    }


    // This would be done by the framework for you
    @Override
    public void onEvent(Event event) {
        switch (event.payload()) {
            case StudentEnrolledFaculty e -> on(e);
            case CourseCreated e -> on(e);
            case CourseCapacityChanged e -> on(e);
            case StudentSubscribed e -> on(e);
            case StudentUnsubscribed e -> on(e);
            default -> {
            }
        }
    }
}