package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.Event;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.sample.faculty.api.command.UnsubscribeStudent;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.List;

import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.courseIdIndex;
import static io.event.thinking.sample.faculty.commandhandler.Indices.studentIdIndex;

public class UnsubscribeStudentCommandHandler implements
        DcbCommandHandler<UnsubscribeStudent, UnsubscribeStudentCommandHandler.State> {

    @Override
    public Criteria criteria(UnsubscribeStudent cmd) {
        return Criteria.criteria(criterion(typeIndex(StudentSubscribed.NAME),
                                           studentIdIndex(cmd.studentId()),
                                           courseIdIndex(cmd.courseId())),
                                 criterion(typeIndex(StudentUnsubscribed.NAME),
                                           studentIdIndex(cmd.studentId()),
                                           courseIdIndex(cmd.courseId())));
    }

    @Override
    public State initialState() {
        return new State(false);
    }

    @Override
    public List<Event> handle(UnsubscribeStudent cmd, State state) {
        state.assertState();
        StudentUnsubscribed payload = new StudentUnsubscribed(cmd.studentId(), cmd.courseId());
        return List.of(event(payload,
                             typeIndex(StudentUnsubscribed.NAME),
                             studentIdIndex(payload.studentId()),
                             courseIdIndex(payload.courseId())));
    }

    // This would be done by the framework for you
    @Override
    public State source(Object event, State state) {
        return switch (event) {
            case StudentSubscribed _ -> state.withSubscribed(true);
            case StudentUnsubscribed _ -> state.withSubscribed(false);
            // since we explicitly define criteria, we don't expect anything else
            default -> throw new RuntimeException("No handler for this event");
        };
    }

    public record State(boolean subscribed) {

        public void assertState() {
            if (!subscribed) {
                throw new RuntimeException("Student is not subscribed to course");
            }
        }

        public State withSubscribed(boolean status) {
            return new State(status);
        }
    }
}
