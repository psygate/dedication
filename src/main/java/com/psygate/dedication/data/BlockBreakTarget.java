package com.psygate.dedication.data;

import java.util.UUID;
import org.bukkit.Material;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class BlockBreakTarget extends MaterialTarget {

    public BlockBreakTarget() {
        super();
    }

    public BlockBreakTarget(Material mat, boolean acceptAny, long value, long target, java.util.UUID UUID) {
        super(mat, acceptAny, value, target, UUID);
    }

    @Override
    public String toString() {
        return "BlockBreakTarget{" + "value=" + getValue() + ", target=" + getTarget() + ", mat=" + getMaterial() + '}';
    }

    @Override
    public BlockBreakTarget copy() {
        return new BlockBreakTarget(getMaterial(), isAcceptAny(), getValue(), getTarget(), getUUID());
    }

    @Override
    public TargetType getType() {
        return TargetType.BLOCK_BREAK;
    }

}
