package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.CommandModel;
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
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Tags.courseIdTag;
import static io.event.thinking.sample.faculty.model.Tags.studentIdTag;

public class SubscribeStudentCommandModel implements CommandModel<SubscribeStudent> {

    private static final int MAX_COURSES_PER_STUDENT = 10;

    private String studentId;
    private String courseId;
    private int noOfCoursesStudentSubscribed;
    private int noOfStudentsSubscribedToCourse;
    private int courseCapacity;
    private boolean alreadySubscribed;

    @Override
    public Criteria criteria(SubscribeStudent cmd) {
        return Criteria.criteria(criterion(type(StudentEnrolledFaculty.NAME), studentIdTag(cmd.studentId())),
                                 criterion(type(CourseCreated.NAME), courseIdTag(cmd.courseId())),
                                 criterion(type(CourseCapacityChanged.NAME), courseIdTag(cmd.courseId())),
                                 criterion(type(StudentSubscribed.NAME), courseIdTag(cmd.courseId())),
                                 criterion(type(StudentSubscribed.NAME), studentIdTag(cmd.studentId())),
                                 criterion(type(StudentUnsubscribed.NAME), courseIdTag(cmd.courseId())),
                                 criterion(type(StudentUnsubscribed.NAME), studentIdTag(cmd.studentId())));
    }

    @Override
    public List<Event> handle(SubscribeStudent cmd) {
        if (studentId == null) {
            throw new RuntimeException("Student with given id never enrolled the faculty");
        }
        if (courseId == null) {
            throw new RuntimeException("Course with given id does not exist");
        }
        if (alreadySubscribed) {
            throw new RuntimeException("Student already subscribed to this course");
        }
        if (noOfStudentsSubscribedToCourse == courseCapacity) {
            throw new RuntimeException("Course is fully booked");
        }
        if (noOfCoursesStudentSubscribed == MAX_COURSES_PER_STUDENT) {
            throw new RuntimeException("Student subscribed to too many courses");
        }
        return List.of(tagEvent(new StudentSubscribed(studentId, courseId)));
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
                     studentIdTag(event.studentId()),
                     courseIdTag(event.courseId()));
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
            default -> throw new RuntimeException("No handler for this event");
        }
    }
}