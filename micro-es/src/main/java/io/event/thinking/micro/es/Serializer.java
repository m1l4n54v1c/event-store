package io.event.thinking.micro.es;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The serializer.
 */
public interface Serializer {

    /**
     * Serializes given {@code obj} to the array of bytes.
     *
     * @param obj the object to be serialized
     * @return serialized array of bytes
     */
    default byte[] serialize(Object obj) {
        var bos = new ByteArrayOutputStream();

        try (var out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Deserializes given array of bytes to the {@link Object}.
     *
     * @param bytes the array of bytes to be deserialized
     * @return the deserialized {@link Object}
     */
    default Object deserialize(byte[] bytes) {
        var bis = new ByteArrayInputStream(bytes);

        try (var in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
