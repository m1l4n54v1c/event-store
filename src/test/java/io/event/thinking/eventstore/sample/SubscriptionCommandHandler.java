package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.Criteria;
import io.event.thinking.eventstore.Event;
import io.event.thinking.eventstore.EventStore;
import io.event.thinking.eventstore.SequencedEvent;
import io.event.thinking.eventstore.Tag;
import reactor.core.publisher.Mono;

import static io.event.thinking.eventstore.ConsistencyCondition.consistencyCondition;
import static io.event.thinking.eventstore.Criteria.criteria;
import static io.event.thinking.eventstore.Criterion.criterion;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.sample.Constants.*;

class SubscriptionCommandHandler {

    private final EventStore eventStore;

    SubscriptionCommandHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    Mono<Long> handle(SubscribeStudent cmd) {
        var criteria = buildCriteria(cmd);
        // source events
        var result = eventStore.read(criteria);
        return result.flux()
                     .map(SequencedEvent::event)
                     // build the command model based on sourced events
                     .reduce(new Subscription(), this::applyEvent)
                     // execute the command
                     .flatMap(Subscription::subscribe)
                     // serialize StudentSubscribed event
                     .thenReturn(new StudentSubscribed(cmd.studentId(), cmd.courseId()).toEvent())
                     // publish the event using consistency marker retrieved while sourcing events
                     .flatMap(e -> eventStore.append(e, consistencyCondition(result.consistencyMarker(), criteria)));
    }

    /**
     * Deserializes the event based on the type, and applies it to the command model (Subscription).
     */
    private Subscription applyEvent(Subscription subscription, Event event) {
        return event.tags()
                    .stream()
                    .filter(t -> EVENT_TYPE.equals(t.key()))
                    .map(Tag::value)
                    .findFirst()
                    .map(type -> switch (type) {
                        case StudentEnrolled.NAME -> subscription.on(StudentEnrolled.from(event.payload()));
                        case CourseCreated.NAME -> subscription.on(CourseCreated.from(event.payload()));
                        case CourseCapacityChanged.NAME -> subscription.on(CourseCapacityChanged.from(event.payload()));
                        case StudentSubscribed.NAME -> subscription.on(StudentSubscribed.from(event.payload()));
                        default -> subscription;
                    }).orElse(subscription);
    }

    private Criteria buildCriteria(SubscribeStudent cmd) {
        return criteria(criterion(tag(EVENT_TYPE, StudentEnrolled.NAME), tag(STUDENT_ID, cmd.studentId())),
                        criterion(tag(EVENT_TYPE, CourseCreated.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(tag(EVENT_TYPE, CourseCapacityChanged.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(tag(EVENT_TYPE, StudentSubscribed.NAME), tag(COURSE_ID, cmd.courseId())),
                        criterion(tag(EVENT_TYPE, StudentSubscribed.NAME), tag(STUDENT_ID, cmd.studentId())));
    }
}