package dev.shadowsoffire.attributeslib.util;

import net.minecraft.entity.LivingEntity;

public interface IEntityOwned {

    public LivingEntity getOwner();

    public void setOwner(LivingEntity owner);
}
