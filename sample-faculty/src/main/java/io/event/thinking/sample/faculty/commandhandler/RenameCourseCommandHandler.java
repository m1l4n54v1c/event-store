package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.RenameCourse;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.CourseRenamed;

import java.util.List;

import static io.event.thinking.eventstore.api.Criterion.allOf;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.courseIdIndex;

public class RenameCourseCommandHandler implements DcbCommandHandler<RenameCourse, RenameCourseCommandHandler.State> {

    /*
        Filter for events, that state whether
        - this course has been created
        - this course has been renamed
     */
    @Override
    public Criteria criteria(RenameCourse command) {
        return anyOf(
                // this course has been created
                allOf(typeIndex(CourseCreated.NAME), courseIdIndex(command.courseId())),
                // this course has been renamed
                allOf(typeIndex(CourseRenamed.NAME), courseIdIndex(command.courseId())));
    }

    @Override
    public State initialState() {
        return State.initial();
    }

    @Override
    public State source(Object event, State state) {
        return switch (event) {
            case CourseCreated e -> state.withCourseCreated(e.id(), e.name());
            case CourseRenamed e -> state.withCourseName(e.newName());
            default -> throw new RuntimeException("No handler for this event");
        };
    }

    @Override
    public List<Event> handle(RenameCourse command, State state) {
        if (command.newName().equals(state.courseName())) {
            throw new RuntimeException("Course already has this name");
        }

        if (state.courseId == null) {
            throw new RuntimeException("Course with given id does not exist");
        }

        return List.of(event(new CourseRenamed(command.courseId(), command.newName()),
                             typeIndex(CourseRenamed.NAME),
                             courseIdIndex(command.courseId())));
    }

    public record State(String courseId, String courseName) {

        public static State initial() {
            return new State(null, null);
        }

        public State withCourseCreated(String courseId, String courseName) {
            return new State(courseId, courseName);
        }

        public State withCourseName(String courseName) {
            return new State(courseId, courseName);
        }
    }
}
