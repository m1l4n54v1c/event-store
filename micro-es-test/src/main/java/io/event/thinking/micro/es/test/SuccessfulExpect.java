package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.api.SequencedEvent;
import io.event.thinking.micro.es.Event;
import io.event.thinking.micro.es.Serializer;

import java.util.Arrays;
import java.util.List;

/**
 * Successful path of the command handling.
 */
public class SuccessfulExpect implements Expect {

    private final long lastGiven;
    private final Serializer serializer;
    private final EventStore eventStore;
    private final MultiEventTagger tagger;

    SuccessfulExpect(Serializer serializer,
                     EventStore eventStore,
                     MultiEventTagger tagger,
                     long lastGiven) {
        this.serializer = serializer;
        this.eventStore = eventStore;
        this.tagger = tagger;
        this.lastGiven = lastGiven;
    }

    @Override
    public void expectNoEvents() {
        if (!publishedEvents().isEmpty()) {
            throw new AssertionError("Expected no events but got " + publishedEvents());
        }
    }

    @Override
    public void expectEvents(Object... events) {
        Event[] tagged = Arrays.stream(events)
                               .map(event -> new Event(tagger.tag(event), event))
                               .toArray(Event[]::new);
        expectEvents(tagged);
    }

    @Override
    public void expectEvents(Event... events) {
        if (!Arrays.stream(events).toList().equals(publishedEvents())) {
            throw new AssertionError("Expected " + Arrays.toString(events) + " but got " + publishedEvents());
        }
    }

    @Override
    public void expectException(Class<? extends Throwable> exceptionType, String message) {
        throw new AssertionError("Expected exception " + exceptionType + " but got " + publishedEvents());
    }

    @Override
    public void expectException() {
        throw new AssertionError("Expected exception but got " + publishedEvents());
    }

    private List<Event> publishedEvents() {
        return eventStore.read(lastGiven + 1)
                         .flux()
                         .map(SequencedEvent::event)
                         .map(e -> new Event(e.tags(), serializer.deserialize(e.payload())))
                         .collectList()
                         .block();
    }
}
