package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.potion.Potion;

public class FlyingEffect extends Potion {

    public FlyingEffect() {
        super(false, 0xFF5555);
        this.registerPotionAttributeModifier(
                ALObjects.Attributes.CREATIVE_FLIGHT.get(),
                "ea575584-4ff4-4c96-a1a3-f2024d9fd898",
                1,
                0);
    }
}
