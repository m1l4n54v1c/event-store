package io.event.thinking.eventstore.inmemory;

import io.event.thinking.eventstore.ConsistencyCondition;
import io.event.thinking.eventstore.Criteria;
import io.event.thinking.eventstore.EventStore;
import io.event.thinking.eventstore.InvalidConsistencyConditionException;
import io.event.thinking.eventstore.SequencedEvent;
import io.event.thinking.eventstore.StampedEvents;
import io.event.thinking.eventstore.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;

/**
 * In-memory implementation of the {@link EventStore}. The implementation is trivial, used to express concepts and for
 * educational purposes. The price that is paid is the poor performance.
 *
 * <p>
 * The implementation uses {@link Schedulers#single()} (which basically uses a single thread) to sequentialize all
 * appends. This choice makes the implementation easy, and lock-free.
 * </p>
 * <p>
 * Data structure for storing all events is a concurrent implementation of the {@link SortedMap}. There are no
 * additional structures (indices) to improve the append/query performance of the Event Store.
 * </p>
 */
public class InMemoryEventStore implements EventStore {

    private final SortedMap<Long, Event> events = new ConcurrentSkipListMap<>();
    private final Scheduler appender = Schedulers.single();

    @Override
    public Mono<Long> append(Event event, ConsistencyCondition consistencyCondition) {
        return Mono.fromSupplier(() -> doAppend(event, consistencyCondition))
                   .subscribeOn(appender);
    }

    @Override
    public StampedEvents read(long fromSequence, Criteria criteria) {
        Supplier<Flux<SequencedEvent>> sourced =
                () -> Flux.fromStream(events.tailMap(fromSequence)
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> criteria.matches(entry.getValue().tags()))
                                            .map(SequencedEvent::sequencedEvent));
        return new StampedEvents(head(), Flux.defer(sourced));
    }

    /**
     * Does the actual append to the in-memory data structure. No need for any synchronization mechanism since appending
     * is done in a single thread.
     */
    private long doAppend(Event event, ConsistencyCondition consistencyCondition) {
        if (consistencyCondition != null && !validate(consistencyCondition)) {
            throw new InvalidConsistencyConditionException();
        }

        long globalSequence = head();
        events.put(globalSequence, event);
        return globalSequence;
    }

    /**
     * Matches the {@code consistencyCondition} with events starting from the consistency marker. If no match is found,
     * the append request is valid.
     */
    private boolean validate(ConsistencyCondition consistencyCondition) {
        return events.tailMap(consistencyCondition.consistencyMarker())
                     .values()
                     .stream()
                     .noneMatch(t -> consistencyCondition.matches(t.tags()));
    }

    /**
     * @return the current head of the Event Store, which is the position of the first event to be appended
     */
    private long head() {
        return events.size();
    }
}