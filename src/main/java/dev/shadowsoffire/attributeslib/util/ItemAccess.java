package dev.shadowsoffire.attributeslib.util;

import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Resolve vanilla's base attack modifier UUIDs without relying on a dev-time access transformer.
 */
public final class ItemAccess {

    private static final UUID BASE_ATTACK_DAMAGE =
            ObfuscationReflectionHelper.getPrivateValue(Item.class, null, "field_111210_e");
    private static final UUID BASE_ATTACK_SPEED =
            ObfuscationReflectionHelper.getPrivateValue(Item.class, null, "field_185050_h");

    private ItemAccess() {}

    public static UUID getBaseAD() {
        return BASE_ATTACK_DAMAGE;
    }

    public static UUID getBaseAS() {
        return BASE_ATTACK_SPEED;
    }
}
