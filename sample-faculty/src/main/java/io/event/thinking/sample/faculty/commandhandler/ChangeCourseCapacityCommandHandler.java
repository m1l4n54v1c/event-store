package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.ChangeCourseCapacity;

import java.util.List;

public class ChangeCourseCapacityCommandHandler implements DcbCommandHandler<ChangeCourseCapacity, ChangeCourseCapacityCommandHandler.State> {

    /*
        We need all events, that show
        - this course has been created
        - any student has subscribed to this course
        - any student has unsubscribed from this course
     */
    @Override
    public Criteria criteria(ChangeCourseCapacity command) {
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
    public List<Event> handle(ChangeCourseCapacity command, State state) {
        return List.of();
    }

    public record State() {

    }
}
