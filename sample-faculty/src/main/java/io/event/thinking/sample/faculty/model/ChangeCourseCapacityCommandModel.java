package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.CommandModel;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.ChangeCourseCapacity;

import java.util.List;

public class ChangeCourseCapacityCommandModel implements CommandModel<ChangeCourseCapacity> {

    @Override
    public Criteria criteria(ChangeCourseCapacity command) {
        // TODO implement
        return null;
    }

    @Override
    public void onEvent(Event event) {
        // TODO implement
    }

    @Override
    public List<Event> handle(ChangeCourseCapacity command) {
        // TODO implement
        return List.of();
    }
}
