package io.event.thinking.eventstore;

import reactor.core.publisher.Flux;

/**
 * A series of events retrieved from the Event Store stamped with the head of the Event Store at the time they were
 * requested. The head could be observed as a version of the Event Store at the requested time.
 *
 * @param head the head of the Event Store - the sequence of the latest stored event in the Event Store. Could be used
 *             as {@link ConsistencyCondition#consistencyMarker()}.
 * @param flux series of events ordered by their sequences
 */
public record StampedEvents(long head, Flux<SequencedEvent> flux) {

}
