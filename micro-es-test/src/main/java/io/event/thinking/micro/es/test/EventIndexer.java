package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.Index;

import java.util.Set;
import java.util.function.Function;

/**
 * A function that returns a {@link Set} of indices based on the {@code event}.
 * @param <T> the type of the event
 */
public interface EventIndexer<T> extends Function<T, Set<Index>> {

    /**
     * Provides a {@link Set} of indices based on the {@code event}.
     * @param event the event
     * @return indices
     */
    Set<Index> index(T event);

    @Override
    default Set<Index> apply(T event) {
        return index(event);
    }
}
