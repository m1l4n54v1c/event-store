package io.event.thinking.eventstore;

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
     * Matches given {@code tags} with the criteria.
     *
     * @param tags a set of tags
     * @return {@code true} if the condition is met, {@code} false otherwise
     */
    public boolean matches(Tag... tags) {
        return matches(Set.of(tags));
    }

    /**
     * Matches given {@code tags} with the criteria.
     *
     * @param tags a set of tags
     * @return {@code true} if the condition is met, {@code} false otherwise
     */
    public boolean matches(Set<Tag> tags) {
        return criteria.matches(tags);
    }
}
