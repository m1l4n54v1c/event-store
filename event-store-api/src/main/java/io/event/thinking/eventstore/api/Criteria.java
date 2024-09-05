package io.event.thinking.eventstore.api;

import java.util.Set;

/**
 * Contains a {@link Set} of {@link Criterion}s. If a single criterion is met within the criteria, we say that the
 * criteria is met as well. In other words, we apply the OR operator in a set of criterions.
 *
 * @param criteria a set of criterions
 * @see Criterion
 */
public record Criteria(Set<Criterion> criteria) {

    /**
     * Factory method for {@link Criteria}.
     *
     * @param criteria an array of criterions
     * @return newly created {@link Criteria}
     */
    public static Criteria criteria(Criterion... criteria) {
        return new Criteria(Set.of(criteria));
    }

    /**
     * Factory method for {@link Criteria}.
     *
     * @param criteria a set of criterions
     * @return newly created {@link Criteria}
     */
    public static Criteria criteria(Set<Criterion> criteria) {
        return new Criteria(criteria);
    }

    /**
     * If any criterion of criterions matches given {@code indices}, the whole criteria match them.
     *
     * @param indices a set of indices
     * @return {@code true} if the criteria matches the {@code indices}, {@code false} otherwise
     */
    public boolean matches(Index... indices) {
        return matches(Set.of(indices));
    }

    /**
     * If any criterion of criterions matches given {@code indices}, the whole criteria match them.
     *
     * @param indices a set of indices
     * @return {@code true} if the criteria matches the {@code indices}, {@code false} otherwise
     */
    public boolean matches(Set<Index> indices) {
        if (criteria.isEmpty()) {
            return true;
        }
        return criteria().stream()
                         .anyMatch(c -> c.matches(indices));
    }
}
