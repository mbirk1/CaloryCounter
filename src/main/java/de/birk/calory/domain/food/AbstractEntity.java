package de.birk.calory.domain.food;

import de.birk.calory.exception.ValidationException;

import java.io.Serializable;

public abstract class AbstractEntity<I> implements Serializable {

    protected abstract I getId();

    protected abstract void validate() throws ValidationException;

    @Override
    public int hashCode() {
        I id = getId();
        return id == null ? super.hashCode() : id.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AbstractEntity)
            || !(object.getClass().isAssignableFrom(getClass()) || getClass().isAssignableFrom(object.getClass()))) {
            return false;
        }

        AbstractEntity<I> entity = (AbstractEntity<I>)object;
        return getId() != null && getId().equals(entity.getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getId();
    }
}
