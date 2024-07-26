package io.event.thinking.eventstore.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static io.event.thinking.eventstore.ConsistencyCondition.consistencyCondition;
import static io.event.thinking.eventstore.Criteria.criteria;
import static io.event.thinking.eventstore.Criterion.criterion;
import static io.event.thinking.eventstore.SequencedEvent.sequencedEvent;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.Event.event;
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
        var criteria = criteria(criterion(aggregateTag));

        var events = eventStore.read(criteria);
        assertEquals(0L, events.head());
        StepVerifier.create(events.flux())
                    .verifyComplete();

        var evt1 = event(payload("evt1"), aggregateTag);
        var append = eventStore.append(evt1, consistencyCondition(events.head(), criteria));
        StepVerifier.create(append)
                    .expectNext(0L)
                    .verifyComplete();

        events = eventStore.read(criteria);
        assertEquals(1L, events.head());
        StepVerifier.create(events.flux())
                    .expectNext(sequencedEvent(0L, evt1))
                    .verifyComplete();

        var evt2 = event(payload("evt2"), aggregateTag);
        append = eventStore.append(evt2, consistencyCondition(events.head(), criteria));
        StepVerifier.create(append)
                    .expectNext(1L)
                    .verifyComplete();

        events = eventStore.read(criteria);
        assertEquals(2L, events.head());
        StepVerifier.create(events.flux())
                    .expectNext(sequencedEvent(0L, evt1),
                                sequencedEvent(1L, evt2))
                    .verifyComplete();
    }

    @Test
    void dcbUseCase() {
        var studentTag = tag("studentId", UUID.randomUUID().toString());
        var courseTag = tag("courseId", UUID.randomUUID().toString());

        var studentCriteria = criteria(criterion(studentTag));
        var courseCriteria = criteria(criterion(courseTag));
        var studentCourseCriteria = criteria(criterion(studentTag, courseTag));

        var studentCreated = event(payload("studentCreated"), studentTag);
        var courseCreated = event(payload("courseCreated"), courseTag);
        var studentEnrolledCourse = event(payload("studentEnrolledCourse"), studentTag, courseTag);

        var head = eventStore.read(studentCriteria).head();
        eventStore.append(studentCreated, consistencyCondition(head, studentCriteria)).block();

        head = eventStore.read(courseCriteria).head();
        eventStore.append(courseCreated, consistencyCondition(head, courseCriteria)).block();

        head = eventStore.read(studentCourseCriteria).head();
        eventStore.append(studentEnrolledCourse, consistencyCondition(head, studentCourseCriteria)).block();

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
