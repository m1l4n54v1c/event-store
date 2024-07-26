package io.event.thinking.eventstore;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.event.thinking.eventstore.Criteria.criteria;
import static io.event.thinking.eventstore.Criterion.criterion;
import static io.event.thinking.eventstore.Tag.tag;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriteriaTest {

    @Test
    void emptyCriteriaMatchWithNoTags() {
        var criteria = criteria();

        assertTrue(criteria.matches());
    }

    @Test
    void emptyCriteriaMatchWithTags() {
        var criteria = criteria();

        assertTrue(criteria.matches(tag("key", "value")));
    }

    @Test
    void criteriaMatchWithNoTags() {
        var criteria = criteria(criterion(tag("key", "value")));

        assertFalse(criteria.matches());
    }

    @Test
    void criteriaExactMatchWithTag() {
        var tag = tag("key", "value");
        var criteria = criteria(criterion(tag));

        assertTrue(criteria.matches(tag));
    }

    @Test
    void criteriaExactMatchWithTags() {
        var tags = Set.of(tag("key1", "value1"), tag("key2", "value2"));
        var criteria = criteria(criterion(tags));

        assertTrue(criteria.matches(tags));
    }

    @Test
    void criteriaDoesntMatchWithLessTags() {
        var tag1 = tag("key1", "value1");
        var tags = Set.of(tag1, tag("key2", "value2"));
        var criteria = criteria(criterion(tags));

        assertFalse(criteria.matches(tag1));
    }

    @Test
    void criteriaMatchesWithMoreTags() {
        var tag1 = tag("key1", "value1");
        var tags = Set.of(tag1, tag("key2", "value2"));
        var criteria = criteria(criterion(tag1));

        assertTrue(criteria.matches(tags));
    }

    @Test
    void criteriaDoesntMatchWithDifferentTags() {
        var tag1 = tag("key1", "value1");
        var tag2 = tag("key2", "value2");
        var criteria = criteria(criterion(tag1));

        assertFalse(criteria.matches(tag2));
    }

    @Test
    void criteriaDoesntMatchWithOneCommonTag() {
        var tag1 = tag("key1", "value1");
        var tag2 = tag("key2", "value2");
        var tag3 = tag("key3", "value3");
        var criteria = criteria(criterion(tag1, tag2));

        assertFalse(criteria.matches(tag1, tag3));
    }
}
