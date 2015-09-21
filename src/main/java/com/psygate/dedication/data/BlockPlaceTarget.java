package com.psygate.dedication.data;

import java.util.UUID;
import org.bukkit.Material;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class BlockPlaceTarget extends MaterialTarget {

    public BlockPlaceTarget() {
        super();
    }

    public BlockPlaceTarget(Material mat, boolean acceptAny, long value, long target, java.util.UUID UUID) {
        super(mat, acceptAny, value, target, UUID);
    }

    @Override
    public String toString() {
        return "BlockPlaceTarget{" + "value=" + getValue() + ", target=" + getTarget() + ", mat=" + getMaterial() + '}';
    }

    @Override
    public BlockPlaceTarget copy() {
        return new BlockPlaceTarget(getMaterial(), isAcceptAny(), getValue(), getTarget(), getUUID());
    }

    @Override
    public TargetType getType() {
        return TargetType.BLOCK_PLACE;
    }

}
