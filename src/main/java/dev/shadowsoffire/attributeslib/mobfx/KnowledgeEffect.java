package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class KnowledgeEffect extends MobEffect {

    public KnowledgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4EE42);
        this.addAttributeModifier(
                ALObjects.Attributes.EXPERIENCE_GAINED.asHolder(),
                AttributesLib.loc("knowledge/experience_gained"),
                Operation.ADD_MULTIPLIED_TOTAL,
                amp -> {
                    int level = amp + 1;
                    return level * level * AttributesLib.knowledgeMult;
                });
    }
}
