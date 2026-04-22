package dev.shadowsoffire.attributeslib.mixin;

import com.mojang.authlib.GameProfile;
import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import dev.shadowsoffire.attributeslib.util.IFlying;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public class PlayerMixin implements IFlying {

    @Shadow public PlayerCapabilities capabilities;

    /**
     * This field records the value of {@link PlayerCapabilities#isFlying} right after NBT read so
     * it can be re-set once attributes are loaded.
     */
    private boolean apoth_flying;

    /**
     * Pipes the owning player into {@link PlayerCapabilities} so {@link Attributes#CREATIVE_FLIGHT}
     * can flip {@code allowFlying} / {@code isFlying}.
     */
    @Inject(
            at = @At(value = "TAIL"),
            method =
                    "<init>(Lnet/minecraft/world/World;Lcom/mojang/authlib/GameProfile;)V",
            require = 1,
            remap = false)
    public void apoth_ownedAbilities(World world, GameProfile profile, CallbackInfo ci) {
        ((IEntityOwned) capabilities).setOwner((EntityLivingBase) (Object) this);
    }

    /**
     * Records {@code isFlying} immediately after NBT deserialization so attribute-provided flight
     * is not lost across login cycles.
     */
    @Inject(
            at = @At(value = "TAIL"),
            method = "readEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V",
            require = 1)
    public void apoth_cacheFlying(NBTTagCompound tag, CallbackInfo ci) {
        if (capabilities.isFlying) {
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

    /**
     * Treats the target as still hit when an auxiliary damage source killed it inside the attack
     * sequence (e.g. bleed/detonation reactions fired from an attribute listener).
     */
    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
                            ordinal = 0),
            method = "attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V",
            require = 1)
    private boolean apoth_handleKilledByAuxDmg(Entity target, DamageSource src, float dmg) {
        boolean res = target.attackEntityFrom(src, dmg);
        return res || target.getEntityData().getBoolean("apoth.killed_by_aux_dmg");
    }
}
