package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.micro.es.CommandBus;
import io.event.thinking.micro.es.Serializer;

/**
 * Command handler invoker. Invokes the {@link io.event.thinking.micro.es.CommandModel#handle(Object)} after the
 * {@link io.event.thinking.micro.es.CommandModel} is sourced.
 *
 * @param <T> the type of the command
 * @see CommandModelFixture
 */
public class When<T> {

    private final Long lastGiven;
    private final CommandBus commandBus;
    private final EventStore eventStore;
    private final Serializer serializer;
    private final MultiEventIndexer indexers;

    When(CommandBus commandBus,
         EventStore eventStore,
         Serializer serializer,
         MultiEventIndexer indexers) {
        this(commandBus, eventStore, serializer, indexers, -1L);
    }

    When(CommandBus commandBus,
         EventStore eventStore,
         Serializer serializer,
         MultiEventIndexer indexers,
         Long lastGiven) {
        this.commandBus = commandBus;
        this.eventStore = eventStore;
        this.serializer = serializer;
        this.indexers = indexers;
        this.lastGiven = lastGiven;
    }

    /**
     * Dispatches the command to the sourced {@link io.event.thinking.micro.es.CommandModel}.
     *
     * @param command the command
     * @return assertions
     */
    public Expect when(T command) {
        try {
            commandBus.dispatch(command)
                      .block();
            return new SuccessfulExpect(serializer, eventStore, indexers, lastGiven);
        } catch (Throwable t) {
            return new ErrorExpect(t);
        }
    }
}
