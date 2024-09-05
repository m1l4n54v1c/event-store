package io.event.thinking.eventstore.api;

/**
 * Represented by its key and value, used to index/tag/mark/associate certain objects with certain concepts. One typical
 * use-case could be indexing events with specific domain concepts.
 *
 * @param key   the key
 * @param value the value
 */
public record Index(String key, String value) {

    /**
     * Factory method for the {@link Index}.
     *
     * @param key   the key
     * @param value the value
     * @return newly created {@link Index}.
     */
    public static Index index(String key, String value) {
        return new Index(key, value);
    }
}
