package dev.shadowsoffire.attributeslib.util;

import net.minecraft.world.damagesource.DamageSource;

public class AttributesUtil {
    public static boolean bypassesResistance(DamageSource damageSource) {
        // 检查具体的 DamageSource 类型
        return damageSource.isMagic() || damageSource.isExplosion() || damageSource.isBypassArmor();
    }

    public static boolean isPhysicalDamage(DamageSource src) {
        return !src.isMagic() && !src.isBypassMagic() && !src.isFire() && !src.isExplosion();
    }
}
