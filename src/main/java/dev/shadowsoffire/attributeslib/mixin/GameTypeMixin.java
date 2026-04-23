package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import dev.shadowsoffire.attributeslib.util.IFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the {@link Attributes#CREATIVE_FLIGHT} attribute modifier whenever the player's
 * {@link GameType} changes (i.e. {@code configurePlayerCapabilities} runs). Mirrors the 1.16
 * reference {@code GameTypeMixin}; the player is recovered from {@link PlayerCapabilities} via
 * {@link IEntityOwned} (set by {@link PlayerMixin}).
 */
@Mixin(GameType.class)
public class GameTypeMixin {

    @Unique private boolean apoth_flying;

    @Inject(
            at = @At("HEAD"),
            method = "configurePlayerCapabilities(Lnet/minecraft/entity/player/PlayerCapabilities;)V",
            require = 1)
    public void apoth_recordOldFlyingAttribs(PlayerCapabilities capabilities, CallbackInfo ci) {
        this.apoth_flying = capabilities.isFlying;
    }

    @Inject(
            at = @At("TAIL"),
            method = "configurePlayerCapabilities(Lnet/minecraft/entity/player/PlayerCapabilities;)V",
            require = 1)
    public void apoth_flightAttribModifier(PlayerCapabilities capabilities, CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) ((IEntityOwned) capabilities).getOwner();
        if (player == null) return;

        GameType self = (GameType) (Object) this;
        boolean creativeOrSpectator = self == GameType.CREATIVE || self == GameType.SPECTATOR;
        AttributeEvents.applyCreativeFlightModifier(player, creativeOrSpectator);

        if (player.getEntityAttribute(Attributes.CREATIVE_FLIGHT.get()).getAttributeValue() > 0) {
            capabilities.allowFlying = true;
            capabilities.isFlying =
                    ((IFlying) player).getAndDestroyFlyingCache() || this.apoth_flying;
        }
    }
}
