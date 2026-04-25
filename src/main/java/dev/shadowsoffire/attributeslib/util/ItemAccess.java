package dev.shadowsoffire.attributeslib.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class ItemAccess extends Item {

    private ItemAccess(Properties pProperties) {
        super(pProperties);
    }

    public static ResourceLocation getBaseAD() {
        return Item.BASE_ATTACK_DAMAGE_ID;
    }

    public static ResourceLocation getBaseAS() {
        return Item.BASE_ATTACK_SPEED_ID;
    }
}
