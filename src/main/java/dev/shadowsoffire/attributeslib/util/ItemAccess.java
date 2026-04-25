package dev.shadowsoffire.attributeslib.util;

import java.util.UUID;
import net.minecraft.item.Item;

public final class ItemAccess extends Item {

    private ItemAccess(Properties pProperties) {
        super(pProperties);
    }

    public static UUID getBaseAD() {
        return Item.ATTACK_DAMAGE_MODIFIER;
    }

    public static UUID getBaseAS() {
        return Item.ATTACK_SPEED_MODIFIER;
    }
}
