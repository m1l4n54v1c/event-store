package io.event.thinking.eventstore.sample.model;

import io.event.thinking.eventstore.Criteria;
import io.event.thinking.eventstore.sample.api.event.StudentSubscribed;
import io.event.thinking.eventstore.sample.api.event.StudentUnsubscribed;
import io.event.thinking.eventstore.sample.api.command.UnsubscribeStudent;
import io.event.thinking.eventstore.sample.microdcb.CommandModel;
import io.event.thinking.eventstore.sample.microdcb.Event;
import reactor.core.publisher.Flux;

import static io.event.thinking.eventstore.Criteria.criteria;
import static io.event.thinking.eventstore.Criterion.criterion;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.sample.microdcb.Event.event;
import static io.event.thinking.eventstore.sample.microdcb.TagUtils.type;
import static io.event.thinking.eventstore.sample.model.Constants.COURSE_ID;
import static io.event.thinking.eventstore.sample.model.Constants.STUDENT_ID;

public class UnsubscribeStudentCommandModel implements CommandModel<UnsubscribeStudent> {

    private boolean subscribed = false;

    @Override
    public Criteria buildCriteria(UnsubscribeStudent cmd) {
        return criteria(criterion(type(StudentSubscribed.NAME),
                                  tag(STUDENT_ID, cmd.studentId()),
                                  tag(COURSE_ID, cmd.courseId())),
                        criterion(type(StudentUnsubscribed.NAME),
                                  tag(STUDENT_ID, cmd.studentId()),
                                  tag(COURSE_ID, cmd.courseId()))
        );
    }

    void on(StudentSubscribed evt) {
        subscribed = true;
    }

    void on(StudentUnsubscribed evt) {
        subscribed = false;
    }

    @Override
    public Flux<Event> handle(UnsubscribeStudent cmd) {
        if (subscribed) {
            return Flux.just(tagEvent(new StudentUnsubscribed(cmd.studentId(), cmd.courseId())));
        }
        return Flux.error(new RuntimeException("Student is not subscribed to course"));
    }

    private static Event tagEvent(StudentUnsubscribed event) {
        return event(event,
                     type(StudentUnsubscribed.NAME),
                     tag(STUDENT_ID, event.studentId()),
                     tag(COURSE_ID, event.courseId()));
    }

    // This would be done by the framework for you
    @Override
    public void onEvent(Event event) {
        switch (event.payload()) {
            case StudentSubscribed e -> on(e);
            case StudentUnsubscribed e -> on(e);
            default -> {
            }
        }
    }
}
