package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Contains a registry of {@link EventTagger}s. Tags any type of the event as long as there is a registered
 * {@link EventTagger} for it.
 */
public class MultiEventTagger implements EventTagger<Object> {

    @SuppressWarnings("rawtypes")
    private final Map<Class, EventTagger> taggers = new HashMap<>();

    /**
     * Registers the tagger for the given {@code type} of the event
     *
     * @param type   the type of the event
     * @param tagger the tagger
     * @param <T>    the type of the event
     * @return this instance for fluent interfacing
     */
    public <T> MultiEventTagger register(Class<T> type, EventTagger<T> tagger) {
        taggers.put(type, tagger);
        return this;
    }

    @Override
    public Set<Tag> tag(Object event) {
        //noinspection unchecked
        return ofNullable(taggers.get(event.getClass()))
                .map(tagger -> tagger.tag(event))
                .orElseThrow(() -> new RuntimeException("No tagger found for " + event.getClass()));
    }
}
