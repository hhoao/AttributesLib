package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.mixin.LivingEntityMixin;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Applied via {@link LivingEntityMixin}
 */
public class SunderingEffect extends MobEffect {

    public SunderingEffect() {
        super(MobEffectCategory.HARMFUL, 0x989898);
    }

}
