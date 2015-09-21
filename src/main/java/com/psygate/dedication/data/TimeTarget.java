package com.psygate.dedication.data;

import java.util.UUID;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class TimeTarget extends NumericTarget {

    public TimeTarget() {
        super();
    }

    public TimeTarget(long value, long target, java.util.UUID UUID) {
        super(value, target, UUID);
    }

    @Override
    public String toString() {
        return "TimeTarget{" + "value=" + getValue() + ", targetInMS=" + getTarget() + '}';
    }

    @Override
    public TargetType getType() {
        return TargetType.TIME;
    }

    @Override
    public Target copy() {
        return new TimeTarget(getValue(), getTarget(), getUUID());
    }

}
