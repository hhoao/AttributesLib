package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.TextFormatting;

public class VitalityEffect extends Effect {

    public VitalityEffect() {
        super(EffectType.BENEFICIAL, TextFormatting.RED.getColor());
        this.addAttributesModifier(
                ALObjects.Attributes.HEALING_RECEIVED.get(),
                "a232ff72-b070-42f5-bf84-bd220d45d698",
                +0.2,
                AttributeModifier.Operation.ADDITION);
    }
}
