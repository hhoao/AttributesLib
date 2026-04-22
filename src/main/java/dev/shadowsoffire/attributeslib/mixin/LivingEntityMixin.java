package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALCombatRules;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import dev.shadowsoffire.attributeslib.util.AttributesUtil;
import dev.shadowsoffire.attributeslib.util.IAttributeManager;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow private AbstractAttributeMap attributeMap;

    public LivingEntityMixin(World world) {
        super(world);
    }

    /**
     * Attaches the entity to its attribute map so {@link AttributeChangedValueEvent} and {@link
     * IAttributeManager} can resolve an owner. Runs at {@code EntityLivingBase} construction TAIL
     * once {@code applyEntityAttributes} has created the map.
     */
    @Inject(
            at = @At(value = "TAIL"),
            method = "<init>(Lnet/minecraft/world/World;)V",
            require = 1,
            remap = false)
    public void apoth_ownedAttrMap(World world, CallbackInfo ci) {
        ((IEntityOwned) attributeMap).setOwner((EntityLivingBase) (Object) this);
    }

    /**
     * Reverses a portion of the Resistance damage reduction when the entity is affected by the
     * Sundering effect. 1.12.2's {@code applyPotionDamageCalculations} has no {@code Math.max}
     * sentinel to hook into, so the boost is applied on RETURN instead.
     */
    @Inject(
            method = "applyPotionDamageCalculations(Lnet/minecraft/util/DamageSource;F)F",
            at = @At("RETURN"),
            cancellable = true,
            require = 1)
    public void apoth_sunderingApplyEffect(
            DamageSource source,
            float damage,
            CallbackInfoReturnable<Float> cir) {
        if (this.isPotionActive(ALObjects.MobEffects.SUNDERING.get())
                && !AttributesUtil.bypassesResistance(source)) {
            int level =
                    this.getActivePotionEffect(ALObjects.MobEffects.SUNDERING.get()).getAmplifier()
                            + 1;
            float value = cir.getReturnValueF();
            value += damage * level * 0.2F;
            cir.setReturnValue(Math.max(value, 0F));
        }
    }

    @Shadow
    public abstract boolean isPotionActive(Potion ef);

    @Shadow
    public abstract PotionEffect getActivePotionEffect(Potion ef);

    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/util/CombatRules;getDamageAfterAbsorb(FFF)F"),
            method = "applyArmorCalculations(Lnet/minecraft/util/DamageSource;F)F",
            require = 1)
    public float apoth_applyArmorPen(
            float amount, float armor, float toughness, DamageSource src, float amt2) {
        return ALCombatRules.getDamageAfterArmor(
                (EntityLivingBase) (Object) this, src, amount, armor, toughness);
    }

    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/util/CombatRules;getDamageAfterMagicAbsorb(FF)F"),
            method = "applyPotionDamageCalculations(Lnet/minecraft/util/DamageSource;F)F",
            require = 1)
    public float apoth_applyProtPen(float amount, float protPoints, DamageSource src, float amt2) {
        return ALCombatRules.getDamageAfterProtection(
                (EntityLivingBase) (Object) this, src, amount, protPoints);
    }

    /**
     * Lock attribute updates during potion-effect change so {@link AttributeChangedValueEvent}
     * doesn't fire midway through add/remove pairs.
     */
    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/potion/Potion;removeAttributesModifiersFromEntity(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/ai/attributes/AbstractAttributeMap;I)V"),
            method = "onChangedPotionEffect(Lnet/minecraft/potion/PotionEffect;Z)V",
            require = 1)
    public void apoth_onEffectUpdateRemoveAttribute(
            PotionEffect effect, boolean forced, CallbackInfo ci) {
        ((IAttributeManager) attributeMap).setAttributesUpdating(true);
    }

    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/potion/Potion;applyAttributesModifiersToEntity(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/ai/attributes/AbstractAttributeMap;I)V",
                            shift = At.Shift.AFTER),
            method = "onChangedPotionEffect(Lnet/minecraft/potion/PotionEffect;Z)V",
            require = 1)
    public void apoth_onEffectUpdateAddAttribute(
            PotionEffect effect, boolean forced, CallbackInfo ci) {
        ((IAttributeManager) attributeMap).setAttributesUpdating(false);
    }
}
