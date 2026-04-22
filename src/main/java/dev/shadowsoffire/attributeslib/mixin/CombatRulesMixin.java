package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALCombatRules;
import net.minecraft.util.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

    /**
     * @author AttributesLib
     * @reason Delegate protection reduction to the configurable {@link ALCombatRules} formula so
     *     protection pierce/shred can be applied upstream.
     * @see ALCombatRules#getDamageAfterProtection
     */
    @Overwrite
    public static float getDamageAfterMagicAbsorb(float damage, float protPoints) {
        return damage * ALCombatRules.getProtDamageReduction(protPoints);
    }

    /**
     * @author AttributesLib
     * @reason Delegate armor reduction to the configurable {@link ALCombatRules} formula so armor
     *     pierce/shred can be applied upstream.
     * @see ALCombatRules#getDamageAfterArmor
     */
    @Overwrite
    public static float getDamageAfterAbsorb(float damage, float armor, float toughness) {
        AttributesLib.LOGGER.trace(
                "Invocation of CombatRules#getDamageAfterAbsorb is bypassing armor pen.");
        return damage * ALCombatRules.getArmorDamageReduction(damage, armor, toughness);
    }
}
