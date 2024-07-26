# Event Store

> Disclaimer: This project is for educational purposes only and represents my own view on the DCB concept.

The simplest possible API and in-memory implementation of an Event Store. The API supports basics of [DCB (Dynamic
Consistency Boundary) concept](https://www.youtube.com/watch?v=IgigmuHHchI).

Although simple, the Event Store API contains enough concepts to cover basic functionalities of an Event Store. It
contains two operations:

* _read_ - reads events from the Event Store based on provided _criteria_.
* _append_ - appends events at the end of the Event Store log. It accepts the _consistency condition_ as the parameter
  used to check the consistency of this _append_.

> This Event Store is missing _stream_ operation which would provide an indefinite stream of events based on certain
> _criteria_.

Before we explore the details of _read_ and _append_ let's explain the necessary terms in order to better understand
mechanics of the Event Store.

| term              | definition                                                                                                                                                                                                            |
|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| _global sequence_ | Each event in the Event Store is associated with _global sequence_ number which determines its position in the globally ordered Event Store log. The _global sequence_ of the very first event is 0.                  |
| _head_            | _global sequence_ of the very first event to be appended to the Event Store                                                                                                                                           |
| _criteria_        | Filters out events from the Event Store. It is composed of _criterions_. Between them, an OR operator is applied - for an event to meet the _criteria_, only one _criterion_ should be satisfied.                     |
| _criterion_       | Integral part of the _criteria_. It is composed of _tags_. Between them, an AND operator is applied - for an event to meet the _criterion_, all of the _tags_ of the event must be matched with the _criterion tags_. |
| _tag_             | Specifies the event in more details. Event Store must store _tags_ together with events and provide a search based on them. Usually, an Event Store index events based on tags for faster retrieval.                  |

## _read_

_read_ operation provides _stamped events_ - all events matching the given _criteria_, starting from the given
_sequence_. These events are _stamped_ with Event Store _head_ at the time they are requested.

## _append_

During the _append_ each event is tagged with a set of tags associating this event with certain concepts from the
_Domain_, or geospatial data, or technical aspects, or anything user-defined. It would be odd to see a tag that does not
already belong to the _payload_ of the event. _append_ accepts the event to be appended (obviously) and the _consistency
condition_ denoting consistency requirements for the _append_. _consistency condition_ is composed of _consistency
marker_ and _criteria_.

_consistency marker_ is telling the Event Store to start searching for events matching given _criteria_ after its
position. If there are no events matching the _criteria_ after the _consistency marker_, consistency condition is
fulfilled, otherwise, it's not.

As a convenience the _read_ operation returns events _stamped_ with the _head_ of the Event Store. The command
model uses _read_ operation to source its state and make the decision based on the state. This decision is usually
appending an event to the Event Store. **This exact command model is going to use the _head_ from the _read_ operation
as the _consistency marker_ for the _append_ operation.**