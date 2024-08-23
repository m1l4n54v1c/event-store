package io.event.thinking.micro.es;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * A bus for dispatching commands to the corresponding {@link CommandModel}.
 */
public interface CommandBus {

    /**
     * Dispatches given {@code command} to the corresponding {@link CommandModel}, if any. Otherwise, returns an errored
     * {@link Mono}.
     *
     * @param command the command
     * @param <T>     the type of the command
     * @return a {@link Mono} of the last persisted event, or an errored {@link Mono} if something goes wrong
     */
    <T> Mono<Long> dispatch(T command);

    /**
     * Registers a factory for the {@link CommandModel} able to handle a command of given {@code commandType}.
     *
     * @param commandType  the type of the command this model is able to handle
     * @param modelFactory the factory to create a fresh {@link CommandModel}
     * @param <T>          the type of the command
     */
    <T> void register(Class<T> commandType, Supplier<CommandModel<T>> modelFactory);
}
