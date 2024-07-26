package io.event.thinking.eventstore;

import java.util.Map;

/**
 * The event with its associated sequence assigned by the Event Store. Inside one event store, the sequence is unique.
 *
 * @param sequence the sequence of the event
 * @param event    the event
 */
public record SequencedEvent(long sequence, Event event) {

    /**
     * Factory method for {@link SequencedEvent}.
     *
     * @param sequence the sequence of the event
     * @param event    the event
     * @return newly created {@link SequencedEvent}
     */
    public static SequencedEvent sequencedEvent(long sequence, Event event) {
        return new SequencedEvent(sequence, event);
    }

    /**
     * Factory method for {@link SequencedEvent}.
     *
     * @param entry a map entry which key is used as the sequence, and value as the event
     * @return newly created {@link SequencedEvent}
     */
    public static SequencedEvent sequencedEvent(Map.Entry<Long, Event> entry) {
        return sequencedEvent(entry.getKey(), entry.getValue());
    }
}
