package io.event.thinking.micro.es.test;

import io.event.thinking.micro.es.Event;

/**
 * Assertion of command handling.
 *
 * @see CommandHandlerFixture
 */
public interface Expect {

    /**
     * Assert that the command didn't produce any events.
     */
    void expectNoEvents();

    /**
     * Assert that the command produced given {@code events}.
     *
     * @param events the events
     */
    void expectEvents(Object... events);

    /**
     * Assert that the command produced given {@code events}.
     *
     * @param events the events
     */
    void expectEvents(Event... events);

    /**
     * Assert that the command rose an exception.
     *
     * @param exceptionType the type of the exception
     * @param message       the exception message
     */
    void expectException(Class<? extends Throwable> exceptionType, String message);

    /**
     * Assert that the command rose an exception.
     */
    void expectException();
}
