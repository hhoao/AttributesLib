package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import dev.shadowsoffire.attributeslib.util.IFlying;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameType.class)
public class GameTypeMixin {

    private boolean apoth_flying;

    @Inject(
            at = @At("HEAD"),
            method = "configurePlayerCapabilities(Lnet/minecraft/entity/player/PlayerAbilities;)V")
    public void apoth_recordOldFlyingAttribs(PlayerAbilities abilities, CallbackInfo ci) {
        this.apoth_flying = abilities.isFlying;
    }

    /**
     * Responsible for applying the creative flight attribute modifier to creative players whenever
     * the game type is updated.<br>
     * Supports {@link Attributes#CREATIVE_FLIGHT}.
     */
    @Inject(
            at = @At("TAIL"),
            method = "configurePlayerCapabilities(Lnet/minecraft/entity/player/PlayerAbilities;)V")
    public void apoth_flightAttribModifier(PlayerAbilities abilities, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) ((IEntityOwned) abilities).getOwner();
        AttributeEvents.applyCreativeFlightModifier(player, (GameType) (Object) this);
        if (player.getAttributeValue(Attributes.CREATIVE_FLIGHT.get()) > 0) {
            abilities.allowFlying = true;
            abilities.isFlying = ((IFlying) player).getAndDestroyFlyingCache() || this.apoth_flying;
        }
    }
}
