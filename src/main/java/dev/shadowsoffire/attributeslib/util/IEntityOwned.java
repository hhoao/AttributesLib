package dev.shadowsoffire.attributeslib.util;

import net.minecraft.entity.EntityLivingBase;

public interface IEntityOwned {

    EntityLivingBase getOwner();

    void setOwner(EntityLivingBase owner);
}
