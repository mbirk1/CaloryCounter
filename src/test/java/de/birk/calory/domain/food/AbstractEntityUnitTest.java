package de.birk.calory.domain.food;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.birk.calory.exception.ValidationException;

public class AbstractEntityUnitTest {

    @Test
    public void hashCodeIsNotNullIdTest() {
        AbstractEntity<Integer> entity = new AbstractEntity<Integer>() {
            @Override
            protected Integer getId() {
                return 1;
            }

            @Override
            protected void validate() throws ValidationException {
                // No validation needed for this test
            }
        };

        int expectedHashCode = 1;
        int actualHashCode = entity.hashCode();

        assertThat(expectedHashCode).isEqualTo(actualHashCode);
    }

    @Test
    public void equalsSameInstanceTest() {
        AbstractEntity<Integer> entity = new AbstractEntity<Integer>() {
            @Override
            protected Integer getId() {
                return 1;
            }

            @Override
            protected void validate() throws ValidationException {
                // No validation needed for this test
            }
        };

        boolean result = entity.equals(entity);

        assertThat(result).isTrue();
    }

    @Test
    public void hashCodehasNullId() {
        AbstractEntity<Integer> entity = new AbstractEntity<Integer>() {
            @Override
            protected Integer getId() {
                return null;
            }

            @Override
            protected void validate() throws ValidationException {
                // No validation needed for this test
            }
        };

        int expectedHashCode = entity.getClass().hashCode();
        int actualHashCode = entity.hashCode();

        assertThat(expectedHashCode).isEqualTo(actualHashCode);
    }

    @Test
    public void equalsNotAbstractEntity() {
        AbstractEntity<Integer> entity = new AbstractEntity<Integer>() {
            @Override
            protected Integer getId() {
                return 1;
            }

            @Override
            protected void validate() throws ValidationException {
                // No validation needed for this test
            }
        };

        boolean result = entity.equals("notAbstractEntity");

        assertThat(result).isFalse();
    }

    @Test
    public void equalsDifferentId() {
        AbstractEntity<Integer> entity1 = new AbstractEntity<Integer>() {
            @Override
            protected Integer getId() {
                return 1;
            }

            @Override
            protected void validate() throws ValidationException {
                // No validation needed for this test
            }
        };

        AbstractEntity<Integer> entity2 = new AbstractEntity<Integer>() {
            @Override
            protected Integer getId() {
                return 2;
            }

            @Override
            protected void validate() throws ValidationException {
                // No validation needed for this test
            }
        };

        boolean result = entity1.equals(entity2);

        assertThat(result).isFalse();
    }

}
