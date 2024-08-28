package io.event.thinking.sample.faculty.model;

import io.event.thinking.eventstore.api.Criteria;
import io.event.thinking.micro.es.CommandModel;
import io.event.thinking.micro.es.Event;
import io.event.thinking.sample.faculty.api.command.ChangeCourseCapacity;
import io.event.thinking.sample.faculty.api.event.CourseCapacityChanged;
import io.event.thinking.sample.faculty.api.event.CourseCreated;
import io.event.thinking.sample.faculty.api.event.StudentSubscribed;
import io.event.thinking.sample.faculty.api.event.StudentUnsubscribed;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.micro.es.Event.event;
import static io.event.thinking.micro.es.Tags.type;
import static io.event.thinking.sample.faculty.model.Tags.courseIdTag;
import static io.event.thinking.sample.faculty.model.Tags.studentIdTag;

public class ChangeCourseCapacityCommandModel implements CommandModel<ChangeCourseCapacity> {

    private String courseId;
    private final List<String> subscribedStudents = new LinkedList<>();

    @Override
    public Criteria criteria(ChangeCourseCapacity cmd) {
        return Criteria.criteria(criterion(type(CourseCreated.NAME), courseIdTag(cmd.courseId())),
                                 criterion(type(StudentSubscribed.NAME), courseIdTag(cmd.courseId())),
                                 criterion(type(StudentUnsubscribed.NAME), courseIdTag(cmd.courseId())));
    }

    void on(CourseCreated evt) {
        this.courseId = evt.id();
    }


    void on(StudentSubscribed evt) {
        subscribedStudents.add(evt.studentId());
    }

    void on(StudentUnsubscribed evt) {
        subscribedStudents.remove(evt.studentId());
    }

    @Override
    public List<Event> handle(ChangeCourseCapacity command) {
        if (command.newCapacity() < 0) {
            throw new RuntimeException("Course with given id does not exist");
        }

        if (courseId == null) {
            throw new RuntimeException("Course with given id does not exist");
        }

        List<Event> events = new ArrayList<>();
        events.add(tagEvent(new CourseCapacityChanged(command.courseId(), command.newCapacity())));
        if (command.newCapacity() < subscribedStudents.size()) {
            subscribedStudents.subList(command.newCapacity(), subscribedStudents.size())
                              .forEach(studentId -> events.add(tagEvent(new StudentUnsubscribed(studentId, courseId))));
        }
        return events;
    }

    private static Event tagEvent(CourseCapacityChanged event) {
        return event(event,
                     type(CourseCapacityChanged.NAME),
                     courseIdTag(event.id()));
    }

    private static Event tagEvent(StudentUnsubscribed event) {
        return event(event,
                     type(StudentUnsubscribed.NAME),
                     courseIdTag(event.courseId()),
                     studentIdTag(event.studentId()));
    }

    @Override
    public void onEvent(Event event) {
        switch (event.payload()) {
            case CourseCreated e -> on(e);
            case StudentSubscribed e -> on(e);
            case StudentUnsubscribed e -> on(e);
            default -> throw new RuntimeException("No handler for this event");
        }
    }
}
