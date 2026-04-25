package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.AttributesLib;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class VitalityEffect extends MobEffect {

    public VitalityEffect() {
        super(MobEffectCategory.BENEFICIAL, ChatFormatting.RED.getColor());
        this.addAttributeModifier(
                ALObjects.Attributes.HEALING_RECEIVED.asHolder(),
                AttributesLib.loc("vitality/healing_received"),
                Operation.ADD_VALUE,
                amp -> 0.2);
    }
}
