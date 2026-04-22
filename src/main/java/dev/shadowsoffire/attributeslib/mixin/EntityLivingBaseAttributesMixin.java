package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.placebo.config.DeferredHelper.RegObj;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers every custom {@link IAttribute} exposed by {@link ALObjects.Attributes} onto each
 * living entity's attribute map once vanilla's own {@code applyEntityAttributes} has completed.
 * Replaces the 1.16 {@code GlobalEntityTypeAttributes} / {@code EntityAttributeCreationEvent}
 * approach, which has no equivalent in 1.12.2.
 */
@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseAttributesMixin {

    @Inject(method = "applyEntityAttributes()V", at = @At("TAIL"), require = 1)
    private void apoth_registerCustomAttributes(CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        for (IAttribute attr : apoth_customAttrs()) {
            if (self.getAttributeMap().getAttributeInstance(attr) == null) {
                self.getAttributeMap().registerAttribute(attr);
            }
        }
    }

    private static IAttribute[] apoth_customAttrs() {
        RegObj<IAttribute>[] regs =
                new RegObj[] {
                    ALObjects.Attributes.ARMOR_PIERCE,
                    ALObjects.Attributes.ARMOR_SHRED,
                    ALObjects.Attributes.ARROW_DAMAGE,
                    ALObjects.Attributes.ARROW_VELOCITY,
                    ALObjects.Attributes.COLD_DAMAGE,
                    ALObjects.Attributes.CRIT_CHANCE,
                    ALObjects.Attributes.CRIT_DAMAGE,
                    ALObjects.Attributes.CURRENT_HP_DAMAGE,
                    ALObjects.Attributes.DODGE_CHANCE,
                    ALObjects.Attributes.DRAW_SPEED,
                    ALObjects.Attributes.EXPERIENCE_GAINED,
                    ALObjects.Attributes.FIRE_DAMAGE,
                    ALObjects.Attributes.GHOST_HEALTH,
                    ALObjects.Attributes.HEALING_RECEIVED,
                    ALObjects.Attributes.LIFE_STEAL,
                    ALObjects.Attributes.MINING_SPEED,
                    ALObjects.Attributes.OVERHEAL,
                    ALObjects.Attributes.PROT_PIERCE,
                    ALObjects.Attributes.PROT_SHRED,
                    ALObjects.Attributes.ELYTRA_FLIGHT,
                    ALObjects.Attributes.CREATIVE_FLIGHT,
                };
        IAttribute[] out = new IAttribute[regs.length];
        for (int i = 0; i < regs.length; i++) {
            out[i] = regs[i].get();
        }
        return out;
    }
}
