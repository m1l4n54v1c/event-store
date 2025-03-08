package io.event.thinking.eventstore.api;

import java.util.Set;

/**
 * A single criterion within {@link Criteria}. Contains a {@link Set} of {@link Tag}s. When matching with another  set
 * of tags, all of the {@link Criterion}'s tags must be contained in the provided set of tags. In other words, we apply
 * the AND boolean operand between these tags.
 *
 * @param tags a set of tags
 */
public record Criterion(Set<Tag> tags) {

    /**
     * Factory method for {@link Criterion}.
     *
     * @param tags an array of tags
     * @return newly created {@link Criterion}
     */
    public static Criterion allOf(Tag... tags) {
        return new Criterion(Set.of(tags));
    }

    /**
     * Factory method for {@link Criterion}.
     *
     * @param tags a set of tags
     * @return newly created {@link Criterion}
     */
    public static Criterion allOf(Set<Tag> tags) {
        return new Criterion(tags);
    }

    /**
     * Matches this criterion with given {@code tags}. This criterion matches given {@code tags} iff all the criterion's
     * tags are contained in the given {@code tags}.
     *
     * @param tags a set of tags
     * @return {@code true} if the criterion matches given {@code tags}, {@code false} otherwise
     */
    public boolean matches(Tag... tags) {
        return matches(Set.of(tags));
    }

    /**
     * Matches this criterion with given {@code tags}. This criterion matches given {@code tags} iff all the criterion's
     * tags are contained in the given {@code tags}.
     *
     * @param tags a set of tags
     * @return {@code true} if the criterion matches given {@code tags}, {@code false} otherwise
     */
    public boolean matches(Set<Tag> tags) {
        if (this.tags.isEmpty()) {
            return true;
        }
        if (tags.isEmpty()) {
            return false;
        }
        return tags.containsAll(this.tags);
    }
}
