package dev.shadowsoffire.attributeslib.util;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/** Accessors for fields that stay non-public in the 1.12.2 dev environment. */
public final class EntityAccess {

    private EntityAccess() {}

    public static int getFireTicks(Entity entity) {
        return ObfuscationReflectionHelper.getPrivateValue(Entity.class, entity, "field_190534_ay");
    }
}
