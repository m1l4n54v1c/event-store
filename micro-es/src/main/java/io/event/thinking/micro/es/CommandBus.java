package io.event.thinking.micro.es;

import reactor.core.publisher.Mono;

/**
 * A bus for dispatching commands to the corresponding {@link DcbCommandHandler}.
 */
public interface CommandBus {

    /**
     * Dispatches given {@code command} to the corresponding {@link DcbCommandHandler}, if any. Otherwise, returns an
     * errored {@link Mono}.
     *
     * @param command the command
     * @param <T>     the type of the command
     * @return a {@link Mono} of the last persisted event, or an errored {@link Mono} if something goes wrong
     */
    <T> Mono<Long> dispatch(T command);

    /**
     * Registers a factory for the {@link DcbCommandHandler} able to handle a command of given {@code commandType}.
     *
     * @param commandType the type of the command this model is able to handle
     * @param handler     the handler of the command that is going to use event sourcing to build the state necessary to
     *                    make the decision
     * @param <C>         the type of the command
     * @param <S>         the type of the state necessary for the command handler to make the decision
     */
    <C, S> void register(Class<C> commandType, DcbCommandHandler<C, S> handler);
}
