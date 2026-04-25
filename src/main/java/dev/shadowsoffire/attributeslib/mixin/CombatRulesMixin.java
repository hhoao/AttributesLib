package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALCombatRules;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

    /**
     * @see {@link ALCombatRules#getDamageAfterProtection(LivingEntity, DamageSource, float, float)}
     */
    @Overwrite
    public static float getDamageAfterMagicAbsorb(float damage, float protPoints) {
        return damage * ALCombatRules.getProtDamageReduction(protPoints);
    }

    /**
     * @see {@link ALCombatRules#getDamageAfterArmor(LivingEntity, DamageSource, float, float,
     *     float)}
     */
    @Overwrite
    public static float getDamageAfterAbsorb(
            LivingEntity target, float damage, DamageSource src, float armor, float toughness) {
        return ALCombatRules.getDamageAfterArmor(target, src, damage, armor, toughness);
    }
}
