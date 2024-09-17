package io.event.thinking.eventstore.api;

import java.util.Set;

/**
 * A single criterion within {@link Criteria}. Contains a {@link Set} of {@link Index}s. When matching with another  set
 * of indices, all of the {@link Criterion}'s indices must be contained in the provided set  of indices. In other words,
 * we apply the AND boolean operand between these indices.
 *
 * @param indices a set of indices
 */
public record Criterion(Set<Index> indices) {

    /**
     * Factory method for {@link Criterion}.
     *
     * @param indices an array of indices
     * @return newly created {@link Criterion}
     */
    public static Criterion allOf(Index... indices) {
        return new Criterion(Set.of(indices));
    }

    /**
     * Factory method for {@link Criterion}.
     *
     * @param indices a set of indices
     * @return newly created {@link Criterion}
     */
    public static Criterion allOf(Set<Index> indices) {
        return new Criterion(indices);
    }

    /**
     * Matches this criterion with given {@code indices}. This criterion matches given {@code indices} iff all the criterion's
     * indices are contained in the given {@code indices}.
     *
     * @param indices a set of indices
     * @return {@code true} if the criterion matches given {@code indices}, {@code false} otherwise
     */
    public boolean matches(Index... indices) {
        return matches(Set.of(indices));
    }

    /**
     * Matches this criterion with given {@code indices}. This criterion matches given {@code indices} iff all the criterion's
     * indices are contained in the given {@code indices}.
     *
     * @param indices a set of indices
     * @return {@code true} if the criterion matches given {@code indices}, {@code false} otherwise
     */
    public boolean matches(Set<Index> indices) {
        if (this.indices.isEmpty()) {
            return true;
        }
        if (indices.isEmpty()) {
            return false;
        }
        return indices.containsAll(this.indices);
    }
}
