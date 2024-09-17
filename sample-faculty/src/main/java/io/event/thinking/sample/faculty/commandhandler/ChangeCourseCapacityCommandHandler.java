package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.ChangeCourseCapacity;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.event.thinking.eventstore.api.Criterion.allOf;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.courseIdIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.studentIdIndex;
import static java.util.Collections.emptyList;

public class ChangeCourseCapacityCommandHandler
        implements DcbCommandHandler<ChangeCourseCapacity, ChangeCourseCapacityCommandHandler.State> {

    /*
        We need all events, that show
        - this course has been created
        - any student has subscribed to this course
        - any student has unsubscribed from this course
     */
    @Override
    public Criteria criteria(ChangeCourseCapacity command) {
        return anyOf(
                // this course has been created
                allOf(typeIndex(CourseCreated.NAME), courseIdIndex(command.courseId())),
                // any student has subscribed to this course
                allOf(typeIndex(StudentSubscribed.NAME), courseIdIndex(command.courseId())),
                // any student has unsubscribed from this course
                allOf(typeIndex(StudentUnsubscribed.NAME), courseIdIndex(command.courseId())));
    }

    @Override
    public State initialState() {
        return State.initial();
    }

    @Override
    public State source(Object event, State state) {
        return switch (event) {
            case CourseCreated e -> state.withCourseId(e.id());
            case StudentSubscribed e -> state.withStudentSubscribed(e.studentId());
            case StudentUnsubscribed e -> state.withStudentUnsubscribed(e.studentId());
            default -> throw new RuntimeException("No handler for this event");
        };
    }

    @Override
    public List<Event> handle(ChangeCourseCapacity command, State state) {
        if (command.newCapacity() < 0) {
            throw new RuntimeException("Course capacity cannot be negative");
        }

        state.assertCourseExists();

        List<Event> events = new ArrayList<>();
        var courseCapacityChanged = new CourseCapacityChanged(command.courseId(), command.newCapacity());
        events.add(event(courseCapacityChanged,
                         typeIndex(CourseCapacityChanged.NAME),
                         courseIdIndex(courseCapacityChanged.id())));
        var subscribedStudents = state.subscribedStudents();
        if (command.newCapacity() < subscribedStudents.size()) {
            subscribedStudents.subList(command.newCapacity(), subscribedStudents.size())
                              .forEach(studentId -> {
                                  var studentSubscribed = new StudentUnsubscribed(studentId, state.courseId());
                                  events.add(event(studentSubscribed,
                                                   typeIndex(StudentUnsubscribed.NAME),
                                                   courseIdIndex(studentSubscribed.courseId()),
                                                   studentIdIndex(studentSubscribed.studentId())));
                              });
        }
        return events;
    }

    public record State(String courseId, List<String> subscribedStudents) {

        public static State initial() {
            return new State(null, emptyList());
        }

        public State withCourseId(String courseId) {
            return new State(courseId, subscribedStudents);
        }

        public State withStudentSubscribed(String studentId) {
            List<String> ids = new LinkedList<>(subscribedStudents);
            ids.add(studentId);
            return new State(courseId, ids);
        }

        public State withStudentUnsubscribed(String studentId) {
            List<String> ids = new LinkedList<>(subscribedStudents);
            ids.remove(studentId);
            return new State(courseId, ids);
        }

        public void assertCourseExists() {
            if (courseId == null) {
                throw new RuntimeException("Course with given id does not exist");
            }
        }
    }
}
