package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Criteria;

import java.util.List;

/**
 * The command handler specific to DCB concept since it uses the {@link Criteria} to define which events are necessary
 * to source the state in order to make the decision. The decision is reflected in events the
 * {@link #handle(Object, Object)} returns. It uses event sourcing to derive the state.
 *
 * @param <C> the type of the command
 * @param <S> the type of the state necessary for this command handler to make the decision
 */
public interface DcbCommandHandler<C, S> {

    /**
     * Criteria necessary to filter events from the event stream in order to build the current state of the command
     * model.
     *
     * @param command the command
     * @return the criteria
     */
    Criteria criteria(C command);

    /**
     * The initial state used as a starting point for applying the sourced events.
     *
     * @return the initial state
     */
    S initialState();

    /**
     * Builds (sources) the state of this handler.
     *
     * @param event the event used to update the state
     * @param state the current state
     * @return the updated state
     */
    S source(Object event, S state);

    /**
     * Handles the given {@code command}. The handler receives the command and the sourced state. Based on these
     * parameters, it returns a list of events representing the effects of the command handling. If command handling is
     * not satisfactory, the handler should throw an exception.
     *
     * @param command the command
     * @param state   event-sourced state
     * @return the list of events representing the effects of command handling
     */
    List<Event> handle(C command, S state);
}
