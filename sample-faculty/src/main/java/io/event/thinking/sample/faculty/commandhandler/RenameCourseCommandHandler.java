package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.RenameCourse;

import java.util.List;

public class RenameCourseCommandHandler implements DcbCommandHandler<RenameCourse, RenameCourseCommandHandler.State> {

    @Override
    public Criteria criteria(RenameCourse command) {
        return null;
    }

    @Override
    public State initialState() {
        return null;
    }

    @Override
    public State source(Object event, State state) {
        return null;
    }

    @Override
    public List<Event> handle(RenameCourse command, State state) {
        return List.of();
    }

    public record State() {

    }
}
