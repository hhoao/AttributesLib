package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.potion.Potion;

public class VitalityEffect extends Potion {

    public VitalityEffect() {
        super(false, 0xFF5555);
        this.setPotionName("effect.attributeslib.vitality");
        this.registerPotionAttributeModifier(
                ALObjects.Attributes.HEALING_RECEIVED.get(),
                "a232ff72-b070-42f5-bf84-bd220d45d698",
                +0.2,
                0);
    }
}
