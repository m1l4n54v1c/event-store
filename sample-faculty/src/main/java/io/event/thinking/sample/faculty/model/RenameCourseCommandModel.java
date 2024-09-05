package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.CommandModel;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.RenameCourse;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.CourseRenamed;

import java.util.List;

import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.model.Indices.courseIdIndex;

public class RenameCourseCommandModel implements CommandModel<RenameCourse> {

    private String courseId;
    private String courseName;

    @Override
    public Criteria criteria(RenameCourse command) {
        return Criteria.criteria(criterion(typeIndex(CourseCreated.NAME), courseIdIndex(command.courseId())),
                                 criterion(typeIndex(CourseRenamed.NAME), courseIdIndex(command.courseId())));
    }

    private void on(CourseCreated evt) {
        courseId = evt.id();
        courseName = evt.name();
    }

    private void on(CourseRenamed evt) {
        courseName = evt.newName();
    }

    @Override
    public void onEvent(Event event) {
        switch (event.payload()) {
            case CourseCreated e -> on(e);
            case CourseRenamed e -> on(e);
            default -> throw new RuntimeException("No handler for this event");
        }
    }

    @Override
    public List<Event> handle(RenameCourse command) {
        if (courseId == null) {
            throw new RuntimeException("Course with given id does not exist");
        }
        if (command.newName().equals(courseName)) {
            throw new RuntimeException("Course already has this name");
        }
        return List.of(tagEvent(new CourseRenamed(courseId, command.newName())));
    }

    private static Event tagEvent(CourseRenamed event) {
        return event(event,
                     typeIndex(CourseRenamed.NAME),
                     courseIdIndex(event.courseId()));
    }
}
