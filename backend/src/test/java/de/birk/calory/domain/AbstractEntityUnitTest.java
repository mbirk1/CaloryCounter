package de.birk.calory.domain;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.exception.ValidationException;

public class AbstractEntityUnitTest {

  @Test
  public void getIdReturnsValidIdTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    String id = entity.getId();

    assertThat(id).isNotNull();
    assertThat(id).isEqualTo("123");
  }

  @Test
  public void equalsReturnsTrueWithSameIdTest() {
    AbstractEntity<String> entity1 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    AbstractEntity<String> entity2 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };
    boolean equals = entity1.equals(entity2);
    assertThat(equals).isTrue();
  }


  @Test
  public void equalsReturnsFalseWithDifferentIdTest() {
    AbstractEntity<String> entity1 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    AbstractEntity<String> entity2 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "456";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };
    boolean equals = entity1.equals(entity2);
    assertThat(equals).isFalse();
  }

  @Test
  public void hashCodeReturnsSameValueWithSameIdTest() {
    AbstractEntity<String> entity1 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    AbstractEntity<String> entity2 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
  }

  @Test
  public void toStringReturnsClassNameAndIdTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    String expected = "#123";
    String actual = entity.toString();

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getIdReturnsNullTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return null;
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    assertThat(entity.getId()).isNull();
  }

  @Test
  public void equalsReturnsFalseWithNullObjectTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    assertThat(entity.equals(null)).isFalse();
  }

  @Test
  public void equalsReturnsFalseWithDifferentClassObjectTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    assertThat(entity.equals("123")).isFalse();
  }

  @Test
  public void equalsSameEntityTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    assertThat(entity.equals(entity)).isTrue();
  }

  @Test
  public void hashCodeReturnsDifferentValueWithDifferentIdTest() {
    AbstractEntity<String> entity1 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "123";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    AbstractEntity<String> entity2 = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return "456";
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
  }

  @Test
  public void toStringReturnsClassNameAndNullWhenIdIsNullTest() {
    AbstractEntity<String> entity = new AbstractEntity<String>() {
      @Override
      protected String getId() {
        return null;
      }

      @Override
      protected void validate() throws ValidationException {
        // implementation not needed for this test
      }
    };

    String expected = "#null";
    String actual = entity.toString();

    assertThat(actual).isEqualTo(expected);
  }

}