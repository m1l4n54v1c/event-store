package io.event.thinking.micro.es.test;

import io.event.thinking.eventstore.api.EventStore;
import io.event.thinking.micro.es.CommandBus;
import io.event.thinking.micro.es.Serializer;

/**
 * Command handler invoker. Invokes the {@link io.event.thinking.micro.es.DcbCommandHandler#handle(Object, Object)}
 * after the {@link io.event.thinking.micro.es.DcbCommandHandler} is sourced.
 *
 * @param <T> the type of the command
 * @see CommandHandlerFixture
 */
public class When<T> {

    private final Long lastGiven;
    private final CommandBus commandBus;
    private final EventStore eventStore;
    private final Serializer serializer;
    private final MultiEventTagger tagger;

    When(CommandBus commandBus,
         EventStore eventStore,
         Serializer serializer,
         MultiEventTagger tagger) {
        this(commandBus, eventStore, serializer, tagger, -1L);
    }

    When(CommandBus commandBus,
         EventStore eventStore,
         Serializer serializer,
         MultiEventTagger tagger,
         Long lastGiven) {
        this.commandBus = commandBus;
        this.eventStore = eventStore;
        this.serializer = serializer;
        this.tagger = tagger;
        this.lastGiven = lastGiven;
    }

    /**
     * Dispatches the command to the sourced {@link io.event.thinking.micro.es.DcbCommandHandler}.
     *
     * @param command the command
     * @return assertions
     */
    public Expect when(T command) {
        try {
            commandBus.dispatch(command)
                      .block();
            return new SuccessfulExpect(serializer, eventStore, tagger, lastGiven);
        } catch (Throwable t) {
            return new ErrorExpect(t);
        }
    }
}
