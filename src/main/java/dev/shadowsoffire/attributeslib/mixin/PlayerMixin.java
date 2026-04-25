package dev.shadowsoffire.attributeslib.mixin;

import com.mojang.authlib.GameProfile;
import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import dev.shadowsoffire.attributeslib.util.IFlying;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerMixin implements IFlying {

    @Shadow public PlayerAbilities abilities;

    /**
     * This field is used to record the value of {@link PlayerAbilities#isFlying} right after
     * deserialization to restore it when attributes are read.
     */
    private boolean apoth_flying;

    /**
     * Constructor mixin to call {@link IEntityOwned#setOwner(LivingEntity)} on {@link #abilities}.
     * <br>
     * Supports {@link Attributes#CREATIVE_FLIGHT}.
     */
    @Inject(
            at = @At(value = "TAIL"),
            method =
                    "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLcom/mojang/authlib/GameProfile;)V",
            require = 1,
            remap = false)
    public void apoth_ownedAbilities(
            World level, BlockPos pos, float yRot, GameProfile profile, CallbackInfo ci) {
        ((IEntityOwned) abilities).setOwner((LivingEntity) (Object) this);
    }

    /**
     * Records the value of {@link net.minecraft.entity.player.PlayerAbilities#isFlying} immediately
     * after deserialization, so it can be re-set when attributes are read.<br>
     * Without this, players with attribute-provided flight will lose it when logging in and logging
     * back out.
     */
    @Inject(
            at = @At(value = "TAIL"),
            method =
                    "Lnet/minecraft/entity/player/PlayerEntity;readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V",
            require = 1)
    public void apoth_cacheFlying(CompoundNBT tag, CallbackInfo ci) {
        if (abilities.isFlying) {
            markFlying();
        }
    }

    @Override
    public boolean getAndDestroyFlyingCache() {
        boolean value = this.apoth_flying;
        this.apoth_flying = false;
        return value;
    }

    @Override
    public void markFlying() {
        this.apoth_flying = true;
    }

    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/LivingEntity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
                            ordinal = 0),
            method = "attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V")
    private boolean apoth_handleKilledByAuxDmg(LivingEntity target, DamageSource src, float dmg) {
        boolean res = target.attackEntityFrom(src, dmg);
        return res || target.getPersistentData().getBoolean("apoth.killed_by_aux_dmg");
    }
}
