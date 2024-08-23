package io.event.thinking.eventstore.api;

/**
 * Represented by its key and value, used to tag/mark/associate certain objects with certain concepts. One typical
 * use-case could be tagging events with specific domain concepts.
 *
 * @param key   the key
 * @param value the value
 */
public record Tag(String key, String value) {

    /**
     * Factory method for the {@link Tag}.
     *
     * @param key   the key
     * @param value the value
     * @return newly created {@link Tag}.
     */
    public static Tag tag(String key, String value) {
        return new Tag(key, value);
    }
}
