package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.EventStore;
import io.event.thinking.eventstore.SequencedEvent;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.event.thinking.eventstore.ConsistencyCondition.consistencyCondition;
import static io.event.thinking.eventstore.Event.event;

/**
 * Dispatches a command to a CommandModel.
 */
public class SimpleCommandDispatcher implements CommandDispatcher {

    @SuppressWarnings("rawtypes")
    private final Map<Class, Supplier<CommandModel>> handlers = new ConcurrentHashMap<>();
    private final EventStore eventStore;
    private final Serializer serializer;

    public SimpleCommandDispatcher(EventStore eventStore) {
        this(eventStore, new Serializer() {
        });
    }

    public SimpleCommandDispatcher(EventStore eventStore, Serializer serializer) {
        this.eventStore = eventStore;
        this.serializer = serializer;
    }

    @Override
    public <T> Mono<Long> dispatch(T cmd) {
        @SuppressWarnings("unchecked")
        CommandModel<T> commandModel = (CommandModel<T>) handlers.get(cmd.getClass()).get();
        var criteria = commandModel.buildCriteria(cmd);
        // source events
        var result = eventStore.read(criteria);
        return result.flux()
                     .map(SequencedEvent::event)
                     // deserialization
                     .map(this::deserialize)
                     // build the command model based on sourced events
                     .reduce(commandModel, (c, e) -> {
                         c.onEvent(e);
                         return c;
                     })
                     // handle the command
                     .map(model -> model.handle(cmd))
                     // serialize
                     .map(this::serialize)
                     // publish the events
                     .flatMap(e -> eventStore.append(e, consistencyCondition(result.consistencyMarker(), criteria)));
    }

    @Override
    public <T> void register(Class<T> commandType, Supplier<CommandModel<T>> model) {
        handlers.put(commandType, model::get);
    }

    private Event deserialize(io.event.thinking.eventstore.Event e) {
        Object payload = serializer.deserialize(e.payload());
        return Event.event(e.tags(), payload);
    }

    private List<io.event.thinking.eventstore.Event> serialize(List<Event> events) {
        return events.stream()
                     .map(this::serialize)
                     .toList();
    }

    private io.event.thinking.eventstore.Event serialize(Event e) {
        byte[] payload = serializer.serialize(e.payload());
        return event(e.tags(), payload);
    }
}