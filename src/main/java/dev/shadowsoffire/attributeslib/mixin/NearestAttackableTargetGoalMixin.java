package dev.shadowsoffire.attributeslib.mixin;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin extends TargetGoal {

    public NearestAttackableTargetGoalMixin(MobEntity pMob, boolean pMustSee) {
        super(pMob, pMustSee);
    }

    @Nullable Predicate<LivingEntity> ctorTargetPredicate;

    @Shadow EntityPredicate targetEntitySelector;

    @Inject(
            method =
                    "<init>(Lnet/minecraft/entity/MobEntity;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V",
            at = @At("TAIL"))
    private void apoth_cachePredicate(
            MobEntity pMob,
            Class<?> pTargetType,
            int pRandomInterval,
            boolean pMustSee,
            boolean pMustReach,
            @Nullable Predicate<LivingEntity> pTargetPredicate,
            CallbackInfo ci) {
        this.ctorTargetPredicate = pTargetPredicate;
    }

    /**
     * Normally, the follow range is encoded into the TargetingConditions at construction time.<br>
     * This means that modifications to it (via attribute modifiers) won't actually change anything.
     * <br>
     * This mixin makes it update before use, so the real value is used.
     *
     * <p>Technically {@link TargetGoal#canContinueToUse()} uses the real value, which should kick
     * it back after a delay.
     */
    @Inject(method = "findNearestTarget()V", at = @At("HEAD"))
    private void apoth_updateFollowRange(CallbackInfo ci) {
        this.targetEntitySelector.setDistance(this.getTargetDistance());
    }
}
