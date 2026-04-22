package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import net.minecraft.entity.ai.attributes.BaseAttribute;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseAttribute.class)
public class AttributeMixin implements IFormattableAttribute {}
