package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.TextFormatting;

public class FlyingEffect extends Effect {

    public FlyingEffect() {
        super(EffectType.BENEFICIAL, TextFormatting.RED.getColor());
        this.addAttributesModifier(
                ALObjects.Attributes.CREATIVE_FLIGHT.get(),
                "ea575584-4ff4-4c96-a1a3-f2024d9fd898",
                1,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public double getAttributeModifierAmount(int pAmplifier, AttributeModifier pModifier) {
        return 1;
    }
}
