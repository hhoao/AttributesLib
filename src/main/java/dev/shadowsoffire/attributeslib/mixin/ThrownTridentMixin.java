package dev.shadowsoffire.attributeslib.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TridentEntity.class)
public abstract class ThrownTridentMixin extends AbstractArrowEntity {

    protected ThrownTridentMixin(EntityType<? extends AbstractArrowEntity> type, World level) {
        super(type, level);
    }

    @ModifyConstant(
            method = "onEntityHit(Lnet/minecraft/util/math/EntityRayTraceResult;)V",
            constant = @Constant(floatValue = 8.0F))
    public float apoth_getTridentDamage(float defaultDmg) {
        return (float) (this.getDamage() * 4.0F);
    }
}
