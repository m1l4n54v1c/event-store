package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.Event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public interface SerializableEvent extends Serializable {

    default byte[] serialize() {
        var bos = new ByteArrayOutputStream();

        try (var out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    Event toEvent();

    static <T> T deserialize(byte[] bytes) {
        var bis = new ByteArrayInputStream(bytes);

        try (var in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
