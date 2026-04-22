package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.mixin.LivingEntityMixin;
import net.minecraft.potion.Potion;

/** Applied via {@link LivingEntityMixin} */
public class SunderingEffect extends Potion {

    public SunderingEffect() {
        super(true, 0x989898);
    }
}
