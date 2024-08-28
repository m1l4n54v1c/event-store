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
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Tags.courseId;

public class RenameCourseCommandModel implements CommandModel<RenameCourse> {

    private String courseId;

    @Override
    public Criteria criteria(RenameCourse command) {
        return Criteria.criteria(criterion(type(CourseCreated.NAME),
                                           courseId(command.courseId())));
    }

    private void on(CourseCreated evt) {
        courseId = evt.id();
    }

    @Override
    public void onEvent(Event event) {
        switch (event.payload()) {
            case CourseCreated e -> on(e);
            default -> throw new RuntimeException("No handler for this event");
        }
    }

    @Override
    public List<Event> handle(RenameCourse command) {
        if (courseId == null) {
            throw new RuntimeException("Course with given id does not exist");
        }
        return List.of(tagEvent(new CourseRenamed(courseId, command.newName())));
    }

    private static Event tagEvent(CourseRenamed event) {
        return event(event,
                     type(CourseRenamed.NAME),
                     courseId(event.courseId()));
    }
}