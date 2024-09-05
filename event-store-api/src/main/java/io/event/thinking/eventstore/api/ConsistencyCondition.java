package io.event.thinking.eventstore.api;

import java.util.Set;

/**
 * The condition used to check the consistency of {@link EventStore#append(Event, ConsistencyCondition)} against the
 * current state of the Event Store.
 *
 * @param consistencyMarker the marker used to validate the consistency condition against the Event Store. If the Event
 *                          Store does not contain any events after the {@code consistencyMarker} matching the given
 *                          {@code criteria}, the condition is satisfied. Otherwise, the Event Store implementation
 *                          should reject the append.
 * @param criteria          the criteria used for matching
 * @see Criteria
 */
public record ConsistencyCondition(long consistencyMarker, Criteria criteria) {

    /**
     * Factory method for {@link ConsistencyCondition}.
     *
     * @param consistencyMarker the marker used to validate the consistency condition against the Event Store. If the Event
     *                          Store does not contain any events after the {@code consistencyMarker} matching the given
     *                          {@code criteria}, the condition is satisfied. Otherwise, the Event Store implementation
     *                          should reject the append.
     * @param criteria          the criteria used for matching
     * @return newly created {@link ConsistencyCondition}
     */
    public static ConsistencyCondition consistencyCondition(long consistencyMarker, Criteria criteria) {
        return new ConsistencyCondition(consistencyMarker, criteria);
    }

    /**
     * Matches given {@code indices} with the criteria.
     *
     * @param indices a set of indices
     * @return {@code true} if the condition is met, {@code} false otherwise
     */
    public boolean matches(Index... indices) {
        return matches(Set.of(indices));
    }

    /**
     * Matches given {@code indices} with the criteria.
     *
     * @param indices a set of indices
     * @return {@code true} if the condition is met, {@code} false otherwise
     */
    public boolean matches(Set<Index> indices) {
        return criteria.matches(indices);
    }
}
