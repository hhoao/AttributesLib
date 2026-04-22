package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link ModifiableAttributeInstance#cachedValue} so we can read the previously-computed
 * value while posting {@link AttributeChangedValueEvent}.
 */
@Mixin(ModifiableAttributeInstance.class)
public interface AttributeInstanceAccessor {

    @Accessor("cachedValue")
    double getCachedValue();
}
