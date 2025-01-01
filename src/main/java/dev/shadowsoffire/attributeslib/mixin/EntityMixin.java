package dev.shadowsoffire.attributeslib.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /**
     * Causes {@link ForgeMod#ENTITY_GRAVITY} to reduce fall damage, equal to the deviation between the current gravity value and the default of 0.08.
     */
    @ModifyVariable(at = @At("HEAD"), method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", argsOnly = true)
    public double apoth_checkFallDamageWithGravity(double yMotion) {
        if (yMotion < 0 && (Object) this instanceof LivingEntity le) {
            double gravity = le.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
            yMotion *= gravity / 0.08D;
            if (gravity <= 0.01D) {
                yMotion = 0; // Slow Falling reduces gravity to this value, and always prevents fall damage.
            }
        }
        return yMotion;
    }

}
