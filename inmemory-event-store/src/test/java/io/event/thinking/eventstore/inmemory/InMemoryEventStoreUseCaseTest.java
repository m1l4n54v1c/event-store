package io.event.thinking.eventstore.inmemory;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.eventstore.api.Criterion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static io.event.thinking.eventstore.api.ConsistencyCondition.consistencyCondition;
import static io.event.thinking.eventstore.api.Criteria.anyOf;
import static io.event.thinking.eventstore.api.Criterion.allOf;
import static io.event.thinking.eventstore.api.SequencedEvent.sequencedEvent;
import static io.event.thinking.eventstore.api.Tag.tag;
import static io.event.thinking.eventstore.api.Event.event;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryEventStoreUseCaseTest {

    private InMemoryEventStore eventStore;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
    }

    @Test
    void aggregateUseCase() {
        var aggregateTag = tag("aggregateId", UUID.randomUUID().toString());
        var criteria = Criteria.anyOf(Criterion.allOf(aggregateTag));

        var events = eventStore.read(criteria);
        assertEquals(0L, events.consistencyMarker());
        StepVerifier.create(events.flux())
                    .verifyComplete();

        var evt1 = event(payload("evt1"), aggregateTag);
        var append = eventStore.append(evt1, consistencyCondition(events.consistencyMarker(), criteria));
        StepVerifier.create(append)
                    .expectNext(0L)
                    .verifyComplete();

        events = eventStore.read(criteria);
        assertEquals(1L, events.consistencyMarker());
        StepVerifier.create(events.flux())
                    .expectNext(sequencedEvent(0L, evt1))
                    .verifyComplete();

        var evt2 = event(payload("evt2"), aggregateTag);
        append = eventStore.append(evt2, consistencyCondition(events.consistencyMarker(), criteria));
        StepVerifier.create(append)
                    .expectNext(1L)
                    .verifyComplete();

        events = eventStore.read(criteria);
        assertEquals(2L, events.consistencyMarker());
        StepVerifier.create(events.flux())
                    .expectNext(sequencedEvent(0L, evt1),
                                sequencedEvent(1L, evt2))
                    .verifyComplete();
    }

    @Test
    void dcbUseCase() {
        var studentTag = tag("studentId", UUID.randomUUID().toString());
        var courseTag = tag("courseId", UUID.randomUUID().toString());

        var studentCriteria = Criteria.anyOf(Criterion.allOf(studentTag));
        var courseCriteria = Criteria.anyOf(Criterion.allOf(courseTag));
        var studentCourseCriteria = Criteria.anyOf(Criterion.allOf(studentTag, courseTag));

        var studentCreated = event(payload("studentCreated"), studentTag);
        var courseCreated = event(payload("courseCreated"), courseTag);
        var studentEnrolledCourse = event(payload("studentEnrolledCourse"), studentTag, courseTag);

        var consistencyMarker = eventStore.read(studentCriteria).consistencyMarker();
        eventStore.append(studentCreated, consistencyCondition(consistencyMarker, studentCriteria)).block();

        consistencyMarker = eventStore.read(courseCriteria).consistencyMarker();
        eventStore.append(courseCreated, consistencyCondition(consistencyMarker, courseCriteria)).block();

        consistencyMarker = eventStore.read(studentCourseCriteria).consistencyMarker();
        eventStore.append(studentEnrolledCourse, consistencyCondition(consistencyMarker, studentCourseCriteria)).block();

        StepVerifier.create(eventStore.read(studentCriteria)
                                      .flux())
                    .expectNext(sequencedEvent(0L, studentCreated),
                                sequencedEvent(2L, studentEnrolledCourse))
                    .verifyComplete();
        StepVerifier.create(eventStore.read(courseCriteria)
                                      .flux())
                    .expectNext(sequencedEvent(1L, courseCreated),
                                sequencedEvent(2L, studentEnrolledCourse))
                    .verifyComplete();
        StepVerifier.create(eventStore.read(studentCourseCriteria)
                                      .flux())
                    .expectNext(sequencedEvent(2L, studentEnrolledCourse))
                    .verifyComplete();
    }

    private static byte[] payload(String payload) {
        return payload.getBytes();
    }
}
