package com.psygate.dedication.data;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public abstract class Target implements Serializable {

    public enum TargetType {

        BLOCK_BREAK,
        BLOCK_PLACE,
        CONSUMPTION,
        TIME
    };

    protected UUID UUID;

    public Target() {
        UUID = new UUID(0, 0);
    }

    public Target(UUID UUID) {
        this.UUID = UUID;
    }

    public UUID getUUID() {
        return UUID;
    }

    public void setUUID(UUID UUID) {
        this.UUID = UUID;
    }

    @Transient
    public abstract boolean isSatisfied();

    public abstract TargetType getType();

    public abstract Target copy();

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.UUID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Target other = (Target) obj;
        if (!Objects.equals(this.UUID, other.UUID)) {
            return false;
        }
        return true;
    }
    
    public abstract void satisfy();
}
