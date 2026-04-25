package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.mixin.LivingEntityMixin;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/** Applied via {@link LivingEntityMixin} */
public class SunderingEffect extends Effect {

    public SunderingEffect() {
        super(EffectType.HARMFUL, 0x989898);
    }
}
