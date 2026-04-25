package dev.shadowsoffire.attributeslib.util;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.Tags;

public class AttributesUtil {

    public static boolean isPhysicalDamage(DamageSource src) {
        return !src.is(Tags.DamageTypes.IS_MAGIC)
                && !src.is(Tags.DamageTypes.IS_TECHNICAL)
                && !src.is(DamageTypes.MAGIC)
                && !src.is(DamageTypes.INDIRECT_MAGIC)
                && !src.is(DamageTypeTags.BYPASSES_ARMOR)
                && !src.is(DamageTypeTags.IS_FIRE)
                && !src.is(DamageTypeTags.IS_EXPLOSION);
    }
}
