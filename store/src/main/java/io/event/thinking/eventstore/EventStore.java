package io.event.thinking.eventstore;

import reactor.core.publisher.Mono;

import java.util.List;

import static io.event.thinking.eventstore.Criteria.criteria;
import static java.util.Collections.emptySet;

/**
 * Defines the API of the Event Store.
 */
public interface EventStore {

    /**
     * Queries the Event Store for events based on the given {@code criteria} starting from the given {@code sequence}.
     * At the time this query is issued, the Event Store captures the consistencyMarker of itself and packages it as a
     * {@link MarkedEvents#consistencyMarker()} in the resulting series of events.
     *
     * @param fromSequence the inclusive starting sequence of the query
     * @param criteria     the criteria used to filter events
     * @return events with the current consistencyMarker of the Event Store
     */
    MarkedEvents read(long fromSequence, Criteria criteria);

    /**
     * Conditionally appends the {@code event} to this Event Store depending on the provided
     * {@code consistencyCondition}.
     *
     * @param event                the event to be stored
     * @param consistencyCondition the consistency condition used to validate the append
     * @return successful {@link Mono} with the global sequence of the stored event if the append was successful. In
     * case the provided {@code consistencyCondition} was not met, it returns errored {@link Mono} with
     * {@link InvalidConsistencyConditionException}.
     * @see ConsistencyCondition
     */
    default Mono<Long> append(Event event, ConsistencyCondition consistencyCondition) {
        return append(List.of(event), consistencyCondition);
    }

    /**
     * Conditionally appends the {@code events} to this Event Store depending on the provided
     * {@code consistencyCondition}.
     *
     * @param events               events to be stored
     * @param consistencyCondition the consistency condition used to validate the append
     * @return successful {@link Mono} with the global sequence of the first stored event if the append was successful.
     * In case the provided {@code consistencyCondition} was not met, it returns errored {@link Mono} with
     * {@link InvalidConsistencyConditionException}.
     * @see ConsistencyCondition
     */
    Mono<Long> append(List<Event> events, ConsistencyCondition consistencyCondition);

    /**
     * Returns all events in the Event Store with the current consistencyMarker.
     *
     * @return events with the current consistencyMarker of the Event Store
     */
    default MarkedEvents read() {
        return read(0L);
    }

    /**
     * Returns all the events starting {@code fromSequence} inclusively with the current consistencyMarker.
     *
     * @param fromSequence inclusive starting sequence
     * @return events with the current consistencyMarker of the Event Store
     */
    default MarkedEvents read(long fromSequence) {
        return read(fromSequence, criteria(emptySet()));
    }

    /**
     * Queries the Event Store for events based on the given {@code criteria} with the current consistencyMarker.
     *
     * @param criteria the criteria used to filter events
     * @return events with the current consistencyMarker of the Event Store
     */
    default MarkedEvents read(Criteria criteria) {
        return read(0L, criteria);
    }

    /**
     * Appends the {@code event} to this Event Store unconditionally.
     *
     * @param event the event to be stored
     * @return a {@link Mono} with the global sequence of the stored event
     */
    default Mono<Long> append(Event event) {
        return append(event, null);
    }
}
