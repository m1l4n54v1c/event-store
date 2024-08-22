package io.event.thinking.eventstore;

/**
 * The exception thrown in the case when the {@link ConsistencyCondition} of the
 * {@link EventStore#append(Event, ConsistencyCondition)} is not met.
 */
public class InvalidConsistencyConditionException extends RuntimeException {

    /**
     * Creates the {@link InvalidConsistencyConditionException}.
     */
    public InvalidConsistencyConditionException() {
        super();
    }
}
