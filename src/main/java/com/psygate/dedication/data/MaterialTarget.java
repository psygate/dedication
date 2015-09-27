package com.psygate.dedication.data;

import java.util.Objects;
import org.bukkit.Material;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public abstract class MaterialTarget extends NumericTarget {

    protected Material mat;
    protected boolean acceptAny;

    public MaterialTarget() {
        super();
        mat = Material.AIR;
        acceptAny = false;
    }

    public MaterialTarget(Material mat, boolean acceptAny, long value, long target, java.util.UUID UUID) {
        super(value, target, UUID);
        Objects.requireNonNull(mat);
        this.mat = mat;
        this.acceptAny = acceptAny;
    }

    public void setMaterial(Material material) {
        this.mat = material;
    }

    public Material getMaterial() {
        return this.mat;
    }

    public Material getMat() {
        return mat;
    }

    public void setMat(Material mat) {
        this.mat = mat;
    }

    public boolean isAcceptAny() {
        return acceptAny;
    }

    public void setAcceptAny(boolean acceptAny) {
        this.acceptAny = acceptAny;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.mat);
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
        final MaterialTarget other = (MaterialTarget) obj;
        if (this.mat != other.mat) {
            return false;
        }
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
