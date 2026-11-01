package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    /**
     * Remembers which entity the incoming packet belongs to. The locals of the target method
     * cannot be captured because its local variable table omits the snapshot iterator slot, so
     * the entity is resolved from the packet instead.
     */
    @Inject(
            at = @At("HEAD"),
            method =
                    "handleEntityProperties(Lnet/minecraft/network/play/server/SPacketEntityProperties;)V",
            require = 1)
    public void apoth_recordEntity(SPacketEntityProperties packet, CallbackInfo ci) {
        World world = Minecraft.getMinecraft().world;
        Entity entity = world == null ? null : world.getEntityByID(packet.getEntityId());
        this.apoth_lastEntity = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }

    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;setBaseValue(D)V"),
            method =
                    "handleEntityProperties(Lnet/minecraft/network/play/server/SPacketEntityProperties;)V",
            require = 1)
    public void apoth_recordOldAttrValue(IAttributeInstance inst, double value) {
        apoth_flushPending();
        if (this.apoth_lastEntity != null) {
            this.apoth_lastInst = inst;
            this.apoth_lastValue = inst.getAttributeValue();
        }
        inst.setBaseValue(value);
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
