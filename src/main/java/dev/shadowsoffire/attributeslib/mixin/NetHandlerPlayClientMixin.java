package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Fires {@link AttributeChangedValueEvent} on the client whenever {@code SPacketEntityProperties}
 * applies a new value for an attribute. Mirrors the 1.16 reference's {@code
 * ClientPacketListenerMixin}, adapted to the 1.12.2 packet flow where each {@code Snapshot}
 * triggers {@code setBaseValue}, {@code removeAllModifiers}, then a loop of {@code applyModifier}.
 *
 * <p>Strategy: at the start of every snapshot we flush any pending event from the previous
 * snapshot (using its now-final value), then stage the current snapshot's pre-update value. A
 * {@code RETURN} hook flushes the very last snapshot.
 */
@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Unique private double apoth_lastValue;
    @Unique @Nullable private IAttributeInstance apoth_lastInst;
    @Unique @Nullable private EntityLivingBase apoth_lastEntity;

    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;setBaseValue(D)V"),
            method =
                    "handleEntityProperties(Lnet/minecraft/network/play/server/SPacketEntityProperties;)V",
            require = 1,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void apoth_recordOldAttrValue(
            SPacketEntityProperties packet,
            CallbackInfo ci,
            Entity entity,
            AbstractAttributeMap map,
            Iterator<?> it,
            SPacketEntityProperties.Snapshot snapshot,
            IAttributeInstance inst) {
        apoth_flushPending();
        if (entity instanceof EntityLivingBase) {
            this.apoth_lastEntity = (EntityLivingBase) entity;
            this.apoth_lastInst = inst;
            this.apoth_lastValue = inst.getAttributeValue();
        }
    }

    @Inject(
            at = @At("RETURN"),
            method =
                    "handleEntityProperties(Lnet/minecraft/network/play/server/SPacketEntityProperties;)V",
            require = 1)
    public void apoth_flushFinalSnapshot(SPacketEntityProperties packet, CallbackInfo ci) {
        apoth_flushPending();
    }

    @Unique
    private void apoth_flushPending() {
        IAttributeInstance inst = this.apoth_lastInst;
        EntityLivingBase entity = this.apoth_lastEntity;
        if (inst != null && entity != null) {
            double newValue = inst.getAttributeValue();
            if (newValue != this.apoth_lastValue) {
                MinecraftForge.EVENT_BUS.post(
                        new AttributeChangedValueEvent(
                                entity, inst, this.apoth_lastValue, newValue));
            }
        }
        this.apoth_lastInst = null;
        this.apoth_lastEntity = null;
    }
}
