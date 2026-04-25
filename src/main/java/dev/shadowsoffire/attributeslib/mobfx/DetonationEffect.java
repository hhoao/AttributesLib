package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class DetonationEffect extends MobEffect {

    public DetonationEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFD800);
    }

    @Override
    public void onMobRemoved(ServerLevel level, LivingEntity entity, int amp, RemovalReason reason) {
        super.onMobRemoved(level, entity, amp, reason);
        int ticks = entity.getRemainingFireTicks();
        if (ticks > 0) {
            entity.setRemainingFireTicks(0);
            entity.hurt(
                    level.damageSources().source(ALObjects.DamageTypes.BLEEDING),
                    (1 + amp) * ticks / 14F);
            AABB bb = entity.getBoundingBox();
            level.sendParticles(
                    ParticleTypes.FLAME,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    100,
                    bb.getXsize(),
                    bb.getYsize(),
                    bb.getZsize(),
                    0.25);
            level.playSound(
                    null,
                    entity,
                    SoundEvents.DRAGON_FIREBALL_EXPLODE,
                    SoundSource.HOSTILE,
                    1,
                    1.2F);
        }
    }
}
