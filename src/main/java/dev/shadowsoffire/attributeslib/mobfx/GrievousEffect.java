package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.TextFormatting;

public class GrievousEffect extends Effect {

    public GrievousEffect() {
        super(EffectType.HARMFUL, TextFormatting.DARK_RED.getColor());
        this.addAttributesModifier(
                ALObjects.Attributes.HEALING_RECEIVED.get(),
                "e04b0b87-5722-4841-bb87-98c6a4632c6f",
                -0.4,
                AttributeModifier.Operation.ADDITION);
    }
}
