package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.AttributesLib;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class FlyingEffect extends MobEffect {

    public FlyingEffect() {
        super(MobEffectCategory.BENEFICIAL, ChatFormatting.RED.getColor());
        this.addAttributeModifier(
                ALObjects.Attributes.CREATIVE_FLIGHT.asHolder(),
                AttributesLib.loc("flying/creative_flight"),
                Operation.ADD_VALUE,
                amp -> 1);
    }
}
