package io.event.thinking.micro.es.test;

import io.event.thinking.micro.es.Event;

import java.util.Arrays;

/**
 * Error path of the command handling.
 */
public class ErrorExpect implements Expect {

    private final Throwable exception;

    ErrorExpect(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public void expectNoEvents() {
        throw new AssertionError("Expected no events but got " + exception);
    }

    @Override
    public void expectEvents(Object... events) {
        throw new AssertionError("Expected " + Arrays.toString(events) + " but got " + exception);
    }

    @Override
    public void expectEvents(Event... events) {
        throw new AssertionError("Expected " + Arrays.toString(events) + " but got " + exception);
    }

    @Override
    public void expectException(Class<? extends Throwable> exceptionType, String message) {
        if (!exceptionType.isInstance(exception) || !message.equals(exception.getMessage())) {
            throw new AssertionError(
                    "Expected " + exceptionType + " with message '" + message + "' but got " + exception);
        }
    }
}
