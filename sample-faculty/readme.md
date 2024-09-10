# Sample - Faculty

Sometimes, the best way to learn a new concept is through a sample. In this sample, we are dealing with the Faculty
Domain. We have students and courses students can subscribe to. Firstly, we must study (no pun intended) the business
requirements. Here they are:

A student can enroll in the faculty. The faculty can decide to create a course with assigned capacity—the number of
students who can subscribe to a course. The capacity of the course must be maintained. After the course has been
created, its capacity can be changed. A student can subscribe to a course and unsubscribe.

Let’s do a short modeling session to identify events.

* `StudentEnrolledFaculty` - a fact that a student has enrolled faculty
* `CourseCreated` - a fact that a course has been created with assigned capacity
* `CourseCapacityChanged` - a fact that the capacity of the course has changed
* `StudentSubscribed` - a fact that the student has subscribed to the course
* `StudentUnsubscribed` - a fact that the student has unsubscribed from the course

We will model only commands for Subscribing to/Unsubscribing from a course for the sample's simplicity.

The `api` package contains corresponding events and commands, and the `commandhandler` package implements the command
handlers.

To avoid boilerplate code as much as possible, there is a thin framework called `micro-es` (micro event-sourcing). It is
neither perfect nor production-ready; its purpose is just to showcase the usage of the DCB in the simplest possible way.
It contains a `DcbCommandHandler` interface, which command handlers should implement. The `DcbCommandHandler` consists
of event-sourcing handlers, the command handler, and the criteria used to filter events for sourcing the state.

> Note that the implementation of this interface does not have to be in the domain itself; it can be in the integration
> layer that would delegate calls to the domain. This way, the domain can remain clean of any framework code. However,
> we decided to implement it right in the domain layer for the simplicity of this sample.

`CommandBus` is also part of the `micro-es` module. It is implemented to find models locally (in the same JVM), source
the state, and dispatch the command to the handler.