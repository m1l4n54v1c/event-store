package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.eventstore.api.SequencedEvent;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.event.thinking.eventstore.api.ConsistencyCondition.consistencyCondition;
import static io.event.thinking.eventstore.api.Event.event;

/**
 * The implementation of {@link CommandBus} that keeps models locally.
 */
public class LocalCommandBus implements CommandBus {

    @SuppressWarnings("rawtypes")
    private final Map<Class, Supplier<CommandModel>> modelFactories = new ConcurrentHashMap<>();
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
        //noinspection unchecked
        return Mono.just(Optional.ofNullable(modelFactories.get(command.getClass()))
                                 .orElseThrow(() -> new RuntimeException("No model found for " + command.getClass())))
                   // create fresh command model
                   .map(factory -> (CommandModel<T>) factory.get())
                   .flatMap(model -> sourceTheModelAndHandleTheCommand(model, command));
    }

    private <T> Mono<Long> sourceTheModelAndHandleTheCommand(CommandModel<T> model, T cmd) {
        var criteria = model.criteria(cmd);
        // source events
        var result = eventStore.read(criteria);
        // keep consistency marker
        var consistencyMarker = result.consistencyMarker();
        return result.flux()
                     .map(SequencedEvent::event)
                     .map(this::deserialize)
                     .reduce(model, this::applyEvent)
                     .map(sourcedModel -> sourcedModel.handle(cmd))
                     .map(this::serialize)
                     .flatMap(events -> publishEvents(events, consistencyMarker, criteria));
    }

    @Override
    public <T> void register(Class<T> commandType, Supplier<CommandModel<T>> modelFactory) {
        modelFactories.put(commandType, modelFactory::get);
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

    private <T> CommandModel<T> applyEvent(CommandModel<T> model, Event event) {
        model.onEvent(event);
        return model;
    }

    private Mono<Long> publishEvents(List<io.event.thinking.eventstore.api.Event> events,
                                     long consistencyMarker,
                                     Criteria criteria) {
        return eventStore.append(events, consistencyCondition(consistencyMarker, criteria));
    }
}