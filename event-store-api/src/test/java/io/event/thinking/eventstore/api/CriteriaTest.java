package io.event.thinking.eventstore.api;

import org.junit.jupiter.api.*;

import java.util.Set;

import static io.event.thinking.eventstore.api.Criterion.allOf;
import static io.event.thinking.eventstore.api.Tag.tag;
import static org.junit.jupiter.api.Assertions.*;

class CriteriaTest {

    @Test
    void emptyCriteriaMatchWithNoTags() {
        var criteria = Criteria.anyOf();

        assertTrue(criteria.matches());
    }

    @Test
    void emptyCriteriaMatchWithTags() {
        var criteria = Criteria.anyOf();

        assertTrue(criteria.matches(tag("key", "value")));
    }

    @Test
    void criteriaMatchWithNoTags() {
        var criteria = Criteria.anyOf(Criterion.allOf(tag("key", "value")));

        assertFalse(criteria.matches());
    }

    @Test
    void criteriaExactMatchWithTag() {
        var tag = tag("key", "value");
        var criteria = Criteria.anyOf(Criterion.allOf(tag));

        assertTrue(criteria.matches(tag));
    }

    @Test
    void criteriaExactMatchWithTags() {
        var tags = Set.of(tag("key1", "value1"), tag("key2", "value2"));
        var criteria = Criteria.anyOf(allOf(tags));

        assertTrue(criteria.matches(tags));
    }

    @Test
    void criteriaDoesntMatchWithLessTags() {
        var tag1 = tag("key1", "value1");
        var tags = Set.of(tag1, tag("key2", "value2"));
        var criteria = Criteria.anyOf(allOf(tags));

        assertFalse(criteria.matches(tag1));
    }

    @Test
    void criteriaMatchesWithMoreTags() {
        var tag1 = tag("key1", "value1");
        var tags = Set.of(tag1, tag("key2", "value2"));
        var criteria = Criteria.anyOf(Criterion.allOf(tag1));

        assertTrue(criteria.matches(tags));
    }

    @Test
    void criteriaDoesntMatchWithDifferentTags() {
        var tag1 = tag("key1", "value1");
        var tag2 = tag("key2", "value2");
        var criteria = Criteria.anyOf(Criterion.allOf(tag1));

        assertFalse(criteria.matches(tag2));
    }

    @Test
    void criteriaDoesntMatchWithOneCommonTag() {
        var tag1 = tag("key1", "value1");
        var tag2 = tag("key2", "value2");
        var tag3 = tag("key3", "value3");
        var criteria = Criteria.anyOf(Criterion.allOf(tag1, tag2));

        assertFalse(criteria.matches(tag1, tag3));
    }
}
