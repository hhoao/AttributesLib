package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Potion;

public class KnowledgeEffect extends Potion {

    public KnowledgeEffect() {
        super(false, 0xF4EE42);
        this.setPotionName("effect.attributeslib.knowledge");
        this.registerPotionAttributeModifier(
                ALObjects.Attributes.EXPERIENCE_GAINED.get(),
                "55688e2f-7db8-4d0b-bc90-eff194546c04",
                AttributesLib.knowledgeMult,
                2);
    }

    @Override
    public double getAttributeModifierAmount(int amp, AttributeModifier modifier) {
        return (++amp * amp) * AttributesLib.knowledgeMult;
    }
}
