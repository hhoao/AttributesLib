package dev.shadowsoffire.attributeslib.mixin.client;

import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import java.util.Iterator;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.play.server.SEntityPropertiesPacket;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetHandler.class)
public class ClientPacketListenerMixin {

    /**
     * Records the old value of the attribute before the attribute packet begins applying new
     * clientside modifiers in {@link
     * ClientPlayNetHandler#handleEntityProperties(SEntityPropertiesPacket)}.
     */
    private double apoth_lastValue;

    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/ai/attributes/ModifiableAttributeInstance;setBaseValue(D)V"),
            method =
                    "handleEntityProperties(Lnet/minecraft/network/play/server/SEntityPropertiesPacket;)V",
            require = 1,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void apoth_recordOldAttrValue(
            SEntityPropertiesPacket packet,
            CallbackInfo ci,
            Entity entity,
            AttributeModifierManager attributemodifiermanager,
            Iterator<SEntityPropertiesPacket.Snapshot> it,
            SEntityPropertiesPacket.Snapshot snapshot,
            ModifiableAttributeInstance inst) {
        this.apoth_lastValue = inst.getValue();
    }

    /**
     * Injected after the for loop iterating {@link
     * SEntityPropertiesPacket.Snapshot#getModifiers()}, which is when after all client attribute
     * modifiers have been cleared and reapplied.
     *
     * <p>Responsible for comparing {@link #apoth_lastValue} to the new value, and posting {@link
     * AttributeChangedValueEvent} if necessary.<br>
     * This is required due to how attributes are synced, since all modifiers are cleared and
     * reapplied instead of only adding/removing modifiers.
     */
    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/ai/attributes/ModifiableAttributeInstance;applyNonPersistentModifier(Lnet/minecraft/entity/ai/attributes/AttributeModifier;)V"),
            method =
                    "handleEntityProperties(Lnet/minecraft/network/play/server/SEntityPropertiesPacket;)V",
            require = 1,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void apoth_postAttrChangedEvent(
            SEntityPropertiesPacket packet,
            CallbackInfo ci,
            Entity entity,
            AttributeModifierManager map,
            Iterator<SEntityPropertiesPacket.Snapshot> it,
            SEntityPropertiesPacket.Snapshot snapshot,
            ModifiableAttributeInstance modifiableattributeinstance) {
        if (modifiableattributeinstance
                != null) { // Due to the loop semantics, the injection point is also the point where
            // the nullcheck will jump to, so we can receive null.
            double newValue = modifiableattributeinstance.getValue();
            if (newValue != apoth_lastValue) {
                MinecraftForge.EVENT_BUS.post(
                        new AttributeChangedValueEvent(
                                (LivingEntity) entity,
                                modifiableattributeinstance,
                                apoth_lastValue,
                                newValue));
            }
        }
    }
}
