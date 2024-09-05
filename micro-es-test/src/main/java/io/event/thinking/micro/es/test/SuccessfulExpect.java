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
    private final MultiEventIndexer indexers;

    SuccessfulExpect(Serializer serializer,
                     EventStore eventStore,
                     MultiEventIndexer indexers,
                     long lastGiven) {
        this.serializer = serializer;
        this.eventStore = eventStore;
        this.indexers = indexers;
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
        Event[] indexed = Arrays.stream(events)
                                .map(event -> new Event(indexers.index(event), event))
                                .toArray(Event[]::new);
        expectEvents(indexed);
    }

    @Override
    public void expectEvents(Event... events) {
        if (!Arrays.stream(events).toList().equals(publishedEvents())) {
            throw new AssertionError("Expected " + publishedEvents() + " but got " + Arrays.toString(events));
        }
    }

    @Override
    public void expectException(Class<? extends Throwable> exceptionType, String message) {
        throw new AssertionError("Expected exception " + exceptionType + " but got " + publishedEvents());
    }

    private List<Event> publishedEvents() {
        return eventStore.read(lastGiven + 1)
                         .flux()
                         .map(SequencedEvent::event)
                         .map(e -> new Event(e.indices(), serializer.deserialize(e.payload())))
                         .collectList()
                         .block();
    }
}
