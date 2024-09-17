package io.event.thinking.sample.faculty.commandhandler;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.DcbCommandHandler;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.UnsubscribeStudent;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.List;

import static io.event.thinking.eventstore.api.Criteria.anyOf;
import static io.event.thinking.eventstore.api.Criterion.allOf;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Indices.typeIndex;
import static io.event.thinking.sample.faculty.commandhandler.FacultyIndices.courseIdIndex;
import static io.event.thinking.sample.faculty.commandhandler.FacultyIndices.studentIdIndex;

public class UnsubscribeStudentCommandHandler implements
        DcbCommandHandler<UnsubscribeStudent, UnsubscribeStudentCommandHandler.State> {

    /*
        We need all events, that show
        - this specific student subscribed to this specific course
        - this specific student unsubscribed from this specific course
     */
    @Override
    public Criteria criteria(UnsubscribeStudent cmd) {
        return anyOf(
                // this student subscribed to this course
                allOf(typeIndex(StudentSubscribed.NAME),
                                studentIdIndex(cmd.studentId()),
                                courseIdIndex(cmd.courseId())),
                // this student unsubscribed from this course
                allOf(typeIndex(StudentUnsubscribed.NAME),
                                studentIdIndex(cmd.studentId()),
                                courseIdIndex(cmd.courseId())));
    }

    @Override
    public State initialState() {
        return new State(false);
    }

    @Override
    public List<Event> handle(UnsubscribeStudent cmd, State state) {
        state.assertStudentSubscribed();
        StudentUnsubscribed payload = new StudentUnsubscribed(cmd.studentId(), cmd.courseId());
        return List.of(event(payload,
                             typeIndex(StudentUnsubscribed.NAME),
                             studentIdIndex(payload.studentId()),
                             courseIdIndex(payload.courseId())));
    }

    @Override
    public State source(Object event, State state) {
        return switch (event) {
            case StudentSubscribed e -> state.withSubscribed(true);
            case StudentUnsubscribed e -> state.withSubscribed(false);
            // since we explicitly define criteria, we don't expect anything else
            default -> throw new RuntimeException("No handler for this event");
        };
    }

    public record State(boolean subscribed) {

        public void assertStudentSubscribed() {
            if (!subscribed) {
                throw new RuntimeException("Student is not subscribed to course");
            }
        }

        public State withSubscribed(boolean status) {
            return new State(status);
        }
    }
}
