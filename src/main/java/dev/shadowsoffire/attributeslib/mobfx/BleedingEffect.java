package dev.shadowsoffire.attributeslib.mobfx;

import static dev.shadowsoffire.attributeslib.impl.AttributeEvents.src;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class BleedingEffect extends Effect {

    public BleedingEffect() {
        super(EffectType.HARMFUL, 0x8B0000);
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        entity.attackEntityFrom(
                src(ALObjects.DamageTypes.BLEEDING, entity.getLastAttackedEntity()),
                1.0F + amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
