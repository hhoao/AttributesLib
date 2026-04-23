package dev.shadowsoffire.attributeslib.mobfx;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.util.EntityAccess;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;

public class DetonationEffect extends Potion {

    public DetonationEffect() {
        super(true, 0xFFD800);
        this.setPotionName("effect.attributeslib.detonation");
    }

    @Override
    public void removeAttributesModifiersFromEntity(
            EntityLivingBase entity, AbstractAttributeMap map, int amp) {
        super.removeAttributesModifiersFromEntity(entity, map, amp);
        int ticks = EntityAccess.getFireTicks(entity);
        if (ticks > 0) {
            entity.extinguish();
            entity.attackEntityFrom(ALObjects.DamageTypes.BLEEDING, (1 + amp) * ticks / 14F);
            if (!entity.world.isRemote) {
                WorldServer level = (WorldServer) entity.world;
                AxisAlignedBB bb = entity.getEntityBoundingBox();
                double xSize = bb.maxX - bb.minX;
                double ySize = bb.maxY - bb.minY;
                double zSize = bb.maxZ - bb.minZ;
                level.spawnParticle(
                        EnumParticleTypes.FLAME,
                        entity.posX,
                        entity.posY,
                        entity.posZ,
                        100,
                        xSize,
                        ySize,
                        zSize,
                        0.25);
                level.playSound(
                        null,
                        entity.posX,
                        entity.posY,
                        entity.posZ,
                        SoundEvents.ENTITY_ENDERDRAGON_FIREBALL_EPLD,
                        SoundCategory.HOSTILE,
                        1,
                        1.2F);
            }
        }
    }
}
