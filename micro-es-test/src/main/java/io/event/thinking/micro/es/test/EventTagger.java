package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.Tag;

import java.util.Set;
import java.util.function.Function;

/**
 * A function that returns a {@link Set} of tags based on the {@code event}.
 * @param <T> the type of the event
 */
public interface EventTagger<T> extends Function<T, Set<Tag>> {

    /**
     * Provides a {@link Set} of tags based on the {@code event}.
     * @param event the event
     * @return tags
     */
    Set<Tag> tag(T event);

    @Override
    default Set<Tag> apply(T event) {
        return tag(event);
    }
}
