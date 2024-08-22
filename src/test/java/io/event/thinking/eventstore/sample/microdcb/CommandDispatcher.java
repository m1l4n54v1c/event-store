package io.event.thinking.eventstore.sample.microdcb;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface CommandDispatcher {

    <T> Mono<Long> dispatch(T cmd);

    <T> void register(Class<T> commandType, Supplier<CommandModel<T>> model);
}
