package io.event.thinking.eventstore.sample;

import reactor.core.publisher.Mono;

class Subscription {

    private static final int MAX_COURSES_PER_STUDENT = 10;

    private String studentId;
    private String courseId;
    private int noOfCoursesStudentSubscribed;
    private int noOfStudentsSubscribedToCourse;
    private int courseCapacity;
    private boolean alreadySubscribed;

    Subscription on(CourseCreated evt) {
        this.courseId = evt.id();
        this.courseCapacity = evt.capacity();
        return this;
    }

    Subscription on(StudentEnrolled evt) {
        this.studentId = evt.id();
        return this;
    }

    Subscription on(CourseCapacityChanged evt) {
        this.courseCapacity = evt.capacity();
        return this;
    }

    Subscription on(StudentSubscribed evt) {
        if (evt.studentId().equals(studentId) && evt.courseId().equals(courseId)) {
            alreadySubscribed = true;
        } else if (evt.studentId().equals(studentId)) {
            noOfCoursesStudentSubscribed++;
        } else {
            noOfStudentsSubscribedToCourse++;
        }
        return this;
    }

    Mono<Void> subscribe() {
        if (studentId == null) {
            return Mono.error(new RuntimeException("Student with given id never enrolled the faculty"));
        }
        if (courseId == null) {
            return Mono.error(new RuntimeException("Course with given id does not exist"));
        }
        if (alreadySubscribed) {
            return Mono.error(new RuntimeException("Student already subscribed to this course"));
        }
        if (noOfStudentsSubscribedToCourse == courseCapacity) {
            return Mono.error(new RuntimeException("Course is fully booked"));
        }
        if (noOfCoursesStudentSubscribed == MAX_COURSES_PER_STUDENT) {
            return Mono.error(new RuntimeException("Student subscribed to too many courses"));
        }
        return Mono.empty();
    }
}