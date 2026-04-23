package dev.shadowsoffire.attributeslib.mobfx;

import static dev.shadowsoffire.attributeslib.impl.AttributeEvents.src;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

public class BleedingEffect extends Potion {

    public BleedingEffect() {
        super(true, 0x8B0000);
        this.setPotionName("effect.attributeslib.bleeding");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        EntityLivingBase attacker = entity.getRevengeTarget();
        DamageSource source = attacker != null
                ? src(ALObjects.DamageTypes.BLEEDING, attacker)
                : ALObjects.DamageTypes.BLEEDING;
        entity.attackEntityFrom(source, 1.0F + amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
