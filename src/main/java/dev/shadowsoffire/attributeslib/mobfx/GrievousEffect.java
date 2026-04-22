package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.potion.Potion;

public class GrievousEffect extends Potion {

    public GrievousEffect() {
        super(true, 0xAA0000);
        this.registerPotionAttributeModifier(
                ALObjects.Attributes.HEALING_RECEIVED.get(),
                "e04b0b87-5722-4841-bb87-98c6a4632c6f",
                -0.4,
                0);
    }
}
