package io.event.thinking.eventstore.api;

import reactor.core.publisher.Flux;

/**
 * A series of events retrieved from the Event Store marked with the consistencyMarker of the Event Store at the time
 * they were requested. The consistencyMarker could be observed as a version of the Event Store at the requested time.
 *
 * <p>
 * One example of the consistency marker would be the head of the Event Store.
 * </p>
 *
 * @param consistencyMarker the consistencyMarker determined by the Event Store. Could be used as
 *                          {@link ConsistencyCondition#consistencyMarker()}.
 * @param flux              series of events ordered by their sequences
 */
public record MarkedEvents(long consistencyMarker, Flux<SequencedEvent> flux) {

}
