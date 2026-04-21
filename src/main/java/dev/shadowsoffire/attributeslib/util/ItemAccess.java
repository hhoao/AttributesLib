package dev.shadowsoffire.attributeslib.util;

import java.util.UUID;
import net.minecraft.item.Item;

/**
 * 1.12.2's {@link Item} exposes the base attack modifier UUIDs as public constants, so we can just
 * forward them directly — no protected access helper needed.
 */
public final class ItemAccess {

    private ItemAccess() {}

    public static UUID getBaseAD() {
        return Item.ATTACK_DAMAGE_MODIFIER;
    }

    public static UUID getBaseAS() {
        return Item.ATTACK_SPEED_MODIFIER;
    }
}
