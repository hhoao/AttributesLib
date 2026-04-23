package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import dev.shadowsoffire.attributeslib.util.IAttributeManager;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Carries the owning {@link EntityLivingBase} reference and fires {@link
 * AttributeChangedValueEvent} whenever the attribute map notifies of a modifier change. Replaces
 * the 1.16 {@code AttributeModifierManagerMixin}, which targeted a class that does not exist in
 * 1.12.2 (the equivalent role here is held by {@code AbstractAttributeMap}).
 *
 * <p>While {@link #apoth_updating} is {@code true} (e.g. during a potion effect
 * remove→add cycle), each affected instance's pre-update value is staged. When the update window
 * closes, the final deltas are posted in a single batch so individual listeners see net changes
 * rather than intermediate noise. This mirrors the 1.16 {@code AttributeModifierManagerMixin}.
 */
@Mixin(AbstractAttributeMap.class)
public abstract class AbstractAttributeMapMixin implements IEntityOwned, IAttributeManager {

    private EntityLivingBase apoth_owner;
    private boolean apoth_updating;
    private final Map<IAttributeInstance, Double> apoth_pending = new HashMap<>();

    @Override
    public EntityLivingBase getOwner() {
        return this.apoth_owner;
    }

    @Override
    public void setOwner(EntityLivingBase owner) {
        if (this.apoth_owner != null)
            throw new UnsupportedOperationException("Cannot set the owner when it is already set.");
        if (owner == null) throw new UnsupportedOperationException("Cannot set the owner to null.");
        this.apoth_owner = owner;
    }

    @Override
    public boolean areAttributesUpdating() {
        return this.apoth_updating;
    }

    @Override
    public void setAttributesUpdating(boolean updating) {
        this.apoth_updating = updating;

        if (this.apoth_updating) {
            this.apoth_pending.clear();
            return;
        }

        if (this.apoth_owner != null && !this.apoth_owner.world.isRemote) {
            for (Map.Entry<IAttributeInstance, Double> entry : this.apoth_pending.entrySet()) {
                IAttributeInstance inst = entry.getKey();
                double oldValue = entry.getValue();
                double newValue = inst.getAttributeValue();
                if (oldValue != newValue) {
                    MinecraftForge.EVENT_BUS.post(
                            new AttributeChangedValueEvent(
                                    this.apoth_owner, inst, oldValue, newValue));
                }
            }
        }
        this.apoth_pending.clear();
    }

    @Inject(
            method =
                    "onAttributeModified(Lnet/minecraft/entity/ai/attributes/IAttributeInstance;)V",
            at = @At("HEAD"),
            require = 1)
    private void apoth_fireAttributeChanged(IAttributeInstance inst, CallbackInfo ci) {
        if (this.apoth_owner == null || this.apoth_owner.world.isRemote) return;

        if (this.apoth_updating) {
            this.apoth_pending.putIfAbsent(
                    inst, ((AttributeInstanceAccessor) inst).getModifiedValue());
            return;
        }

        double oldValue = ((AttributeInstanceAccessor) inst).getModifiedValue();
        double newValue = inst.getAttributeValue();
        if (oldValue != newValue) {
            MinecraftForge.EVENT_BUS.post(
                    new AttributeChangedValueEvent(this.apoth_owner, inst, oldValue, newValue));
        }
    }
}
