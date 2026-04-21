package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALCombatRules;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import dev.shadowsoffire.attributeslib.util.AttributesUtil;
import dev.shadowsoffire.attributeslib.util.IAttributeManager;
import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow private AttributeModifierManager attributes;

    public LivingEntityMixin(EntityType<?> pEntityType, World pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Constructor mixin to call {@link IEntityOwned#setOwner(LivingEntity)} on {@link #attributes}.
     * <br>
     * Supports {@link AttributeChangedValueEvent}.
     */
    @Inject(
            at = @At(value = "TAIL"),
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V",
            require = 1,
            remap = false)
    public void apoth_ownedAttrMap(EntityType<?> type, World level, CallbackInfo ci) {
        ((IEntityOwned) attributes).setOwner((LivingEntity) (Object) this);
    }

    /**
     * @author Shadows
     * @reason Injection of the Sundering potion effect, which is applied during resistance
     *     calculations.
     * @param value Damage modifier percentage after resistance has been applied [1.0, -inf]
     * @param max Zero
     * @param source The damage source
     * @param damage The initial damage amount
     */
    @Redirect(
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"),
            method = "applyPotionDamageCalculations(Lnet/minecraft/util/DamageSource;F)F")
    public float apoth_sunderingApplyEffect(
            float value, float max, DamageSource source, float damage) {
        if (this.isPotionActive(ALObjects.MobEffects.SUNDERING.get())
                && !AttributesUtil.bypassesResistance(source)) {
            int level =
                    this.getActivePotionEffect(ALObjects.MobEffects.SUNDERING.get()).getAmplifier()
                            + 1;
            value += damage * level * 0.2F;
        }
        return Math.max(value, max);
    }

    /**
     * @author Shadows
     * @reason Used to enter an if-condition so the above mixin always triggers.
     */
    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/entity/LivingEntity;isPotionActive(Lnet/minecraft/potion/Effect;)Z"),
            method = "applyPotionDamageCalculations(Lnet/minecraft/util/DamageSource;F)F")
    public boolean apoth_sunderingHasEffect(LivingEntity ths, Effect effect) {
        return true;
    }

    /**
     * @author Shadows
     * @reason Used to prevent an NPE since we're faking true on hasEffect
     */
    @Redirect(
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/potion/EffectInstance;getAmplifier()I"),
            method = "applyPotionDamageCalculations(Lnet/minecraft/util/DamageSource;F)F")
    public int apoth_sunderingGetAmplifier(@Nullable EffectInstance inst) {
        return inst == null ? -1 : inst.getAmplifier();
    }

    @Shadow
    public abstract boolean isPotionActive(Effect ef);

    @Shadow
    public abstract EffectInstance getActivePotionEffect(Effect ef);

    @Shadow
    public abstract void setOnGround(boolean p_21182_);

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
                (LivingEntity) (Object) this, src, amount, armor, toughness);
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
                (LivingEntity) (Object) this, src, amount, protPoints);
    }

    /**
     * @author ChampionAsh5357
     * @reason Lock attribute updates for event until after new modifiers are added
     */
    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/potion/Effect;removeAttributesModifiersFromEntity(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/ai/attributes/AttributeModifierManager;I)V"),
            method = "onChangedPotionEffect(Lnet/minecraft/potion/EffectInstance;Z)V",
            require = 1)
    public void apoth_onEffectUpdateRemoveAttribute(
            EffectInstance pEffectInstance, boolean pForced, CallbackInfo ci) {
        ((IAttributeManager) attributes).setAttributesUpdating(true);
    }

    /**
     * @author ChampionAsh5357
     * @reason Unlock attribute updates for event until after new modifiers are added
     */
    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/potion/Effect;applyAttributesModifiersToEntity(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/ai/attributes/AttributeModifierManager;I)V",
                            shift = At.Shift.AFTER),
            method = "onChangedPotionEffect(Lnet/minecraft/potion/EffectInstance;Z)V",
            require = 1)
    public void apoth_onEffectUpdateAddAttribute(
            EffectInstance pEffectInstance, boolean pForced, CallbackInfo ci) {
        ((IAttributeManager) attributes).setAttributesUpdating(false);
    }
}
