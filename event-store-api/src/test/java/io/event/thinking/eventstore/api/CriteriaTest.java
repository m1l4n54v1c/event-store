package io.event.thinking.eventstore.api;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.event.thinking.eventstore.api.Criteria.criteria;
import static io.event.thinking.eventstore.api.Criterion.criterion;
import static io.event.thinking.eventstore.api.Index.index;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriteriaTest {

    @Test
    void emptyCriteriaMatchWithNoIndices() {
        var criteria = criteria();

        assertTrue(criteria.matches());
    }

    @Test
    void emptyCriteriaMatchWithIndices() {
        var criteria = criteria();

        assertTrue(criteria.matches(index("key", "value")));
    }

    @Test
    void criteriaMatchWithNoIndices() {
        var criteria = criteria(criterion(index("key", "value")));

        assertFalse(criteria.matches());
    }

    @Test
    void criteriaExactMatchWithIndex() {
        var index = index("key", "value");
        var criteria = criteria(criterion(index));

        assertTrue(criteria.matches(index));
    }

    @Test
    void criteriaExactMatchWithIndices() {
        var indices = Set.of(index("key1", "value1"), index("key2", "value2"));
        var criteria = criteria(criterion(indices));

        assertTrue(criteria.matches(indices));
    }

    @Test
    void criteriaDoesntMatchWithLessIndices() {
        var index1 = index("key1", "value1");
        var indices = Set.of(index1, index("key2", "value2"));
        var criteria = criteria(criterion(indices));

        assertFalse(criteria.matches(index1));
    }

    @Test
    void criteriaMatchesWithMoreIndices() {
        var index1 = index("key1", "value1");
        var indices = Set.of(index1, index("key2", "value2"));
        var criteria = criteria(criterion(index1));

        assertTrue(criteria.matches(indices));
    }

    @Test
    void criteriaDoesntMatchWithDifferentIndices() {
        var index1 = index("key1", "value1");
        var index2 = index("key2", "value2");
        var criteria = criteria(criterion(index1));

        assertFalse(criteria.matches(index2));
    }

    @Test
    void criteriaDoesntMatchWithOneCommonIndex() {
        var index1 = index("key1", "value1");
        var index2 = index("key2", "value2");
        var index3 = index("key3", "value3");
        var criteria = criteria(criterion(index1, index2));

        assertFalse(criteria.matches(index1, index3));
    }
}
