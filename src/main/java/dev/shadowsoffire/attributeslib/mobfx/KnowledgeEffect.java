package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class KnowledgeEffect extends Effect {

    public KnowledgeEffect() {
        super(EffectType.BENEFICIAL, 0xF4EE42);
        this.addAttributesModifier(
                ALObjects.Attributes.EXPERIENCE_GAINED.get(),
                "55688e2f-7db8-4d0b-bc90-eff194546c04",
                AttributesLib.knowledgeMult,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public double getAttributeModifierValue(int amp, AttributeModifier modifier) {
        return (++amp * amp) * AttributesLib.knowledgeMult;
    }
}
