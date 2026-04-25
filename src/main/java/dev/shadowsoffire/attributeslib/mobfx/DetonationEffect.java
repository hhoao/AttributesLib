package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

public class DetonationEffect extends Effect {

    public DetonationEffect() {
        super(EffectType.HARMFUL, 0xFFD800);
    }

    @Override
    public void removeAttributesModifiersFromEntity(
            LivingEntity entity, AttributeModifierManager map, int amp) {
        super.removeAttributesModifiersFromEntity(entity, map, amp);
        int ticks = entity.getFireTimer();
        if (ticks > 0) {
            entity.setFire(0);
            entity.attackEntityFrom(ALObjects.DamageTypes.BLEEDING, (1 + amp) * ticks / 14F);
            ServerWorld level = (ServerWorld) entity.world;
            AxisAlignedBB bb = entity.getBoundingBox();
            level.spawnParticle(
                    ParticleTypes.FLAME,
                    entity.getPosX(),
                    entity.getPosY(),
                    entity.getPosZ(),
                    100,
                    bb.getXSize(),
                    bb.getYSize(),
                    bb.getZSize(),
                    0.25);
            level.playSound(
                    null,
                    entity.getPosX(),
                    entity.getPosY(),
                    entity.getPosZ(),
                    SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE,
                    SoundCategory.HOSTILE,
                    1,
                    1.2F);
        }
    }
}
