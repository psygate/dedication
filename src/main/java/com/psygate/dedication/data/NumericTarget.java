package com.psygate.dedication.data;

import java.util.Objects;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public abstract class NumericTarget extends Target {

    protected long value;
    protected long target;

    public NumericTarget() {
        super();
        value = 0;
        target = 0;
    }

    public NumericTarget(long value, long target, java.util.UUID UUID) {
        super(UUID);
        this.value = value;
        this.target = target;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void increment(long value) {
        this.value += value;
    }

    public long getValue() {
        return value;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    @Override
    public void satisfy() {
        this.value = this.target;
    }

    @Override
    public boolean isSatisfied() {
        return value >= target;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 61 * hash + (int) (this.target ^ (this.target >>> 32));
        hash = 61 * hash + Objects.hashCode(this.UUID);

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
        final NumericTarget other = (NumericTarget) obj;
        if (this.value != other.value) {
            return false;
        }
        if (this.target != other.target) {
            return false;
        }
        if (!Objects.equals(this.UUID, other.UUID)) {
            return false;
        }
        return true;
    }

}
