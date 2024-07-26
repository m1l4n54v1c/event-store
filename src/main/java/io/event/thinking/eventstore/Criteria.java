package io.event.thinking.eventstore;

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
     * If any criterion of criterions matches given {@code tags}, the whole criteria match them.
     *
     * @param tags a set of tags
     * @return {@code true} if the criteria matches the {@code tags}, {@code false} otherwise
     */
    public boolean matches(Tag... tags) {
        return matches(Set.of(tags));
    }

    /**
     * If any criterion of criterions matches given {@code tags}, the whole criteria match them.
     *
     * @param tags a set of tags
     * @return {@code true} if the criteria matches the {@code tags}, {@code false} otherwise
     */
    public boolean matches(Set<Tag> tags) {
        if (criteria.isEmpty()) {
            return true;
        }
        return criteria().stream()
                         .anyMatch(c -> c.matches(tags));
    }
}
