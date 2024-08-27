package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.CommandModel;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.RenameCourse;

import java.util.List;

public class RenameCourseCommandModel implements CommandModel<RenameCourse> {

    @Override
    public Criteria criteria(RenameCourse command) {
        //TODO implement
        return null;
    }

    @Override
    public void onEvent(Event event) {
        //TODO implement
    }

    @Override
    public List<Event> handle(RenameCourse command) {
        //TODO implement
        return List.of();
    }
}
