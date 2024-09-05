package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.inmemory.InMemoryEventStore;
import io.event.thinking.micro.es.CommandBus;
import io.event.thinking.micro.es.CommandModel;
import io.event.thinking.micro.es.Event;
import io.event.thinking.micro.es.LocalCommandBus;
import io.event.thinking.micro.es.Serializer;

import java.util.Arrays;

import static io.event.thinking.eventstore.api.Event.event;

/**
 * Test fixture for {@link CommandModel}s.
 *
 * @param <T> the type of the command
 */
public class CommandModelFixture<T> {

    private final CommandBus commandBus;
    private final EventStore eventStore;
    private final Serializer serializer = new Serializer() {
    };
    private final MultiEventIndexer indexers;

    /**
     * Instantiates this fixture.
     *
     * @param commandType       the type of the command
     * @param commandModel      the command model
     * @param multiEventIndexer the indexer
     */
    public CommandModelFixture(Class<T> commandType,
                               CommandModel<T> commandModel,
                               MultiEventIndexer multiEventIndexer) {
        this.eventStore = new InMemoryEventStore();
        this.commandBus = new LocalCommandBus(eventStore, serializer);
        this.indexers = multiEventIndexer;

        commandBus.register(commandType, () -> commandModel);
    }

    /**
     * Appends given {@code events} to the event stream.
     *
     * @param events the events to append to the event stream
     * @return {@link When} continuation
     */
    public When<T> given(Object... events) {
        Event[] indexed = Arrays.stream(events)
                                .map(event -> new Event(indexers.index(event), event))
                                .toArray(Event[]::new);
        return given(indexed);
    }

    /**
     * Appends given {@code events} to the event stream.
     *
     * @param events the events to append to the event stream
     * @return {@link When} continuation
     */
    public When<T> given(Event... events) {
        var eventList = Arrays.stream(events)
                              .map(event -> event(event.indices(), serializer.serialize(event.payload())))
                              .toList();
        Long lastGiven = eventStore.append(eventList)
                                   .block();
        return new When<>(commandBus, eventStore, serializer, indexers, lastGiven);
    }

    /**
     * Starts the fixture with an empty event stream.
     *
     * @return {@link When} continuation
     */
    public When<T> givenNoEvents() {
        return new When<>(commandBus, eventStore, serializer, indexers);
    }
}
