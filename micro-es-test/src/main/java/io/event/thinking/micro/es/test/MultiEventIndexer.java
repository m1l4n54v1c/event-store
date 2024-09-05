package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.Index;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Contains a registry of {@link EventIndexer}s. Indexes any type of the event as long as there is a registered
 * {@link EventIndexer} for it.
 */
public class MultiEventIndexer implements EventIndexer<Object> {

    @SuppressWarnings("rawtypes")
    private final Map<Class, EventIndexer> indexers = new HashMap<>();

    /**
     * Registers the indexer for the given {@code type} of the event
     *
     * @param type    the type of the event
     * @param indexer the indexer
     * @param <T>     the type of the event
     * @return this instance for fluent interfacing
     */
    public <T> MultiEventIndexer register(Class<T> type, EventIndexer<T> indexer) {
        indexers.put(type, indexer);
        return this;
    }

    @Override
    public Set<Index> index(Object event) {
        //noinspection unchecked
        return ofNullable(indexers.get(event.getClass()))
                .map(indexer -> indexer.index(event))
                .orElseThrow(() -> new RuntimeException("No indexer found for " + event.getClass()));
    }
}
