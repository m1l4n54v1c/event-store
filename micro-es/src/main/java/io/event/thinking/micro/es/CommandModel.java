package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Criteria;

import java.util.List;

/**
 * Defines a model for a given command that is event-sourced. Implementations should define the {@link Criteria} on how
 * to filter event stream for events, event-sourcing handlers, and the command handler.
 *
 * @param <T> the type of the command, this model is capable of handling
 */
public interface CommandModel<T> {

    /**
     * The criteria based on which the event stream is going to be filtered to source this model.
     *
     * @param command the command used to build the criteria
     * @return the criteria
     */
    Criteria criteria(T command);

    /**
     * Event-sourcing handler. It'll receive events used to source this model which are filtered based on the
     * {@link #criteria(Object)}. The implementation must be deterministic.
     *
     * @param event the event to source this model
     */
    void onEvent(Event event);

    /**
     * Command handler. Once this model is sourced, it'll be given the command to handle. The result of command handling
     * is represented by the list of events returned by this handler.
     *
     * @param command the command to handle
     * @return a list of events to be published
     */
    List<Event> handle(T command);
}