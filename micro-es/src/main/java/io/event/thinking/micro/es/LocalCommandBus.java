package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.api.SequencedEvent;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.event.thinking.eventstore.api.ConsistencyCondition.consistencyCondition;
import static io.event.thinking.eventstore.api.Event.event;

/**
 * The implementation of {@link CommandBus} that keeps handlers locally.
 */
public class LocalCommandBus implements CommandBus {

    @SuppressWarnings("rawtypes")
    private final Map<Class, DcbCommandHandler> handlers = new ConcurrentHashMap<>();
    private final EventStore eventStore;
    private final Serializer serializer;

    /**
     * Instantiates this bus with the given {@code eventStore} and uses Java serializer.
     *
     * @param eventStore the event store
     */
    public LocalCommandBus(EventStore eventStore) {
        this(eventStore, new Serializer() {
        });
    }

    /**
     * Instantiates this bus with the given {@code eventStore} and {@code serializer}.
     *
     * @param eventStore the event store
     * @param serializer the serializer
     */
    public LocalCommandBus(EventStore eventStore, Serializer serializer) {
        this.eventStore = eventStore;
        this.serializer = serializer;
    }

    @Override
    public <T> Mono<Long> dispatch(T command) {
        return Mono.just(Optional.ofNullable(handlers.get(command.getClass()))
                                 .orElseThrow(() -> new RuntimeException("No model found for " + command.getClass())))
                   .flatMap(handler -> {
                       var model = handler.initialState();
                       //noinspection unchecked
                       var criteria = handler.criteria(command);
                       var result = eventStore.read(criteria);
                       var consistencyMarker = result.consistencyMarker();
                       //noinspection unchecked
                       return result.flux()
                                    .map(SequencedEvent::event)
                                    .map(this::deserialize)
                                    .reduce(model, (m, event) -> handler.source(event.payload(), m))
                                    .map(sourcedModel -> handler.handle(command, sourcedModel))
                                    .map(this::serialize)
                                    .flatMap(events -> publishEvents(events, consistencyMarker, criteria));
                   });
    }

    @Override
    public <C, S> void register(Class<C> commandType, DcbCommandHandler<C, S> handler) {
        handlers.put(commandType, handler);
    }

    private Event deserialize(io.event.thinking.eventstore.api.Event e) {
        Object payload = serializer.deserialize(e.payload());
        return Event.event(e.indices(), payload);
    }

    private List<io.event.thinking.eventstore.api.Event> serialize(List<Event> events) {
        return events.stream()
                     .map(this::serialize)
                     .toList();
    }

    private io.event.thinking.eventstore.api.Event serialize(Event e) {
        byte[] payload = serializer.serialize(e.payload());
        return event(e.indices(), payload);
    }

    private Mono<Long> publishEvents(List<io.event.thinking.eventstore.api.Event> events,
                                     long consistencyMarker,
                                     Criteria criteria) {
        return eventStore.append(events, consistencyCondition(consistencyMarker, criteria));
    }
}