package dev.shadowsoffire.attributeslib.util;

import net.minecraft.util.DamageSource;

public class AttributesUtil {
    public static boolean bypassesResistance(DamageSource damageSource) {
        // 检查具体的 DamageSource 类型
        return damageSource.isMagicDamage()
                || damageSource.isExplosion()
                || damageSource.isUnblockable();
    }

    public static boolean isPhysicalDamage(DamageSource src) {
        return !src.isMagicDamage()
                && !src.isUnblockable()
                && !src.isFireDamage()
                && !src.isExplosion();
    }
}
