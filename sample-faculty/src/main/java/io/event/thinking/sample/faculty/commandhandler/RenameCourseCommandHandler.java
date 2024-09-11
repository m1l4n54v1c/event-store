package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.RenameCourse;

import java.util.List;

public class RenameCourseCommandHandler implements DcbCommandHandler<RenameCourse, RenameCourseCommandHandler.State> {

    /*
        Filter for events, that state whether
        - this course has been created
        - this course has been renamed
     */
    @Override
    public Criteria criteria(RenameCourse command) {
        // TODO: Build criteria
        return null;
    }

    @Override
    public State initialState() {
        // TODO: Construct initial state
        return new State();
    }

    @Override
    public State source(Object event, State state) {
        // TODO: Source state from events
        return state;
    }

    @Override
    public List<Event> handle(RenameCourse command, State state) {
        // TODO: Handle the command
        return List.of();
    }

    public record State() {

    }
}
