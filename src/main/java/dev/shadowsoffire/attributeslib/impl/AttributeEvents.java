package dev.shadowsoffire.attributeslib.impl;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.attributeslib.packet.CritParticleMessage;
import dev.shadowsoffire.attributeslib.util.AttributesUtil;
import dev.shadowsoffire.attributeslib.util.IFlying;
import dev.shadowsoffire.placebo.network.PacketDistro;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AttributeEvents {

    private boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ItemBow;
    }

    /**
     * Implementation for {@link ALObjects.Attributes#DRAW_SPEED}.
     */
    @SubscribeEvent
    public void drawSpeed(LivingEntityUseItemEvent.Tick e) {
        if (e.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e.getEntity();
            double t = player.getEntityAttribute(ALObjects.Attributes.DRAW_SPEED.get())
                            .getAttributeValue() - 1;
            if (t == 0 || !this.canBenefitFromDrawSpeed(e.getItem())) return;

            int offset = -1;
            if (t < 0) {
                offset = 1;
                t = -t;
            }

            while (t > 1) {
                e.setDuration(e.getDuration() + offset);
                t--;
            }

            if (t > 0.5F) {
                if (e.getEntity().ticksExisted % 2 == 0) e.setDuration(e.getDuration() + offset);
                t -= 0.5F;
            }

            int mod = (int) Math.floor(1 / Math.min(1, t));
            if (e.getEntity().ticksExisted % mod == 0) e.setDuration(e.getDuration() + offset);
            t--;
        }
    }

    /** Manages the Life Steal and Overheal attributes. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void lifeStealOverheal(LivingHurtEvent e) {
        if (e.getSource().getImmediateSource() instanceof EntityLivingBase
                && AttributesUtil.isPhysicalDamage(e.getSource())) {
            EntityLivingBase attacker = (EntityLivingBase) e.getSource().getImmediateSource();
            float lifesteal = (float) attacker.getEntityAttribute(ALObjects.Attributes.LIFE_STEAL.get())
                    .getAttributeValue();
            float dmg = Math.min(e.getAmount(), e.getEntityLiving().getHealth());
            if (lifesteal > 0.001) {
                attacker.heal(dmg * lifesteal);
            }
            float overheal = (float) attacker.getEntityAttribute(ALObjects.Attributes.OVERHEAL.get())
                    .getAttributeValue();
            float maxOverheal = attacker.getMaxHealth() * 0.5F;
            if (overheal > 0 && attacker.getAbsorptionAmount() < maxOverheal) {
                attacker.setAbsorptionAmount(
                        Math.min(maxOverheal, attacker.getAbsorptionAmount() + dmg * overheal));
            }
        }
    }

    /** Recursion guard for {@link #meleeDamageAttributes(LivingAttackEvent)}. */
    private static boolean noRecurse = false;

    /**
     * Applies melee damage attributes: CURRENT_HP_DAMAGE, FIRE_DAMAGE, COLD_DAMAGE.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void meleeDamageAttributes(LivingAttackEvent e) {
        if (e.getEntityLiving().world.isRemote || e.getEntityLiving().getHealth() <= 0) return;
        if (noRecurse) return;
        noRecurse = true;
        if (e.getSource().getImmediateSource() instanceof EntityLivingBase
                && AttributesUtil.isPhysicalDamage(e.getSource())) {
            EntityLivingBase attacker = (EntityLivingBase) e.getSource().getImmediateSource();
            float hpDmg = (float) attacker.getEntityAttribute(ALObjects.Attributes.CURRENT_HP_DAMAGE.get())
                    .getAttributeValue();
            float fireDmg = (float) attacker.getEntityAttribute(ALObjects.Attributes.FIRE_DAMAGE.get())
                    .getAttributeValue();
            float coldDmg = (float) attacker.getEntityAttribute(ALObjects.Attributes.COLD_DAMAGE.get())
                    .getAttributeValue();
            EntityLivingBase target = e.getEntityLiving();
            int time = target.hurtResistantTime;
            target.hurtResistantTime = 0;
            if (hpDmg > 0.001 && AttributesLib.localAtkStrength >= 0.85F) {
                target.attackEntityFrom(
                        src(ALObjects.DamageTypes.CURRENT_HP_DAMAGE, attacker),
                        AttributesLib.localAtkStrength * hpDmg * target.getHealth());
            }
            target.hurtResistantTime = 0;
            if (fireDmg > 0.001 && AttributesLib.localAtkStrength >= 0.55F) {
                target.attackEntityFrom(
                        src(ALObjects.DamageTypes.FIRE_DAMAGE, attacker),
                        AttributesLib.localAtkStrength * fireDmg);
                target.setFire((int) (10 * fireDmg));
            }
            target.hurtResistantTime = 0;
            if (coldDmg > 0.001 && AttributesLib.localAtkStrength >= 0.55F) {
                target.attackEntityFrom(
                        src(ALObjects.DamageTypes.COLD_DAMAGE, attacker),
                        AttributesLib.localAtkStrength * coldDmg);
                target.addPotionEffect(
                        new PotionEffect(
                                MobEffects.SLOWNESS,
                                (int) (15 * coldDmg),
                                MathHelper.floor(coldDmg / 5)));
            }
            target.hurtResistantTime = time;
            if (target.getHealth() <= 0) {
                target.getEntityData().setBoolean("apoth.killed_by_aux_dmg", true);
            }
        }
        noRecurse = false;
    }

    public static DamageSource src(DamageSource type, EntityLivingBase entity) {
        return new EntityDamageSource(type.getDamageType(), entity);
    }

    /** Handles CRIT_CHANCE and CRIT_DAMAGE. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void apothCriticalStrike(LivingHurtEvent e) {
        EntityLivingBase attacker =
                e.getSource().getImmediateSource() instanceof EntityLivingBase
                        ? (EntityLivingBase) e.getSource().getImmediateSource()
                        : null;
        if (attacker == null) return;

        double critChance = attacker.getEntityAttribute(ALObjects.Attributes.CRIT_CHANCE.get())
                .getAttributeValue();
        float critDmg = (float) attacker.getEntityAttribute(ALObjects.Attributes.CRIT_DAMAGE.get())
                .getAttributeValue();

        ThreadLocalRandom current = ThreadLocalRandom.current();

        float critMult = 1.0F;

        while (current.nextFloat() <= critChance && critDmg > 1.0F) {
            critChance--;
            critMult *= critDmg;
            critDmg *= 0.85F;
        }

        e.setAmount(e.getAmount() * critMult);

        if (critMult > 1 && !attacker.world.isRemote) {
            PacketDistro.sendToTracking(
                    AttributesLib.CHANNEL,
                    new CritParticleMessage(e.getEntity().getEntityId()),
                    (WorldServer) attacker.world,
                    e.getEntity().getPosition());
        }
    }

    /** CRIT_DAMAGE interaction with vanilla critical strikes. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void vanillaCritDmg(CriticalHitEvent e) {
        float critDmg = (float) e.getEntityLiving()
                .getEntityAttribute(ALObjects.Attributes.CRIT_DAMAGE.get())
                .getAttributeValue();
        if (e.isVanillaCritical()) {
            e.setDamageModifier(Math.max(e.getDamageModifier(), critDmg));
        }
    }

    /** Handles MINING_SPEED. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void breakSpd(BreakSpeed e) {
        e.setNewSpeed(
                e.getNewSpeed()
                        * (float) e.getEntityLiving()
                                .getEntityAttribute(ALObjects.Attributes.MINING_SPEED.get())
                                .getAttributeValue());
    }

    /** Block-break XP handling for EXPERIENCE_GAINED. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void blockBreak(BlockEvent.BreakEvent e) {
        double xpMult = e.getPlayer()
                .getEntityAttribute(ALObjects.Attributes.EXPERIENCE_GAINED.get())
                .getAttributeValue();
        e.setExpToDrop((int) (e.getExpToDrop() * xpMult));
    }

    /** Mob-drop XP handling for EXPERIENCE_GAINED. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void mobXp(LivingExperienceDropEvent e) {
        EntityPlayer player = e.getAttackingPlayer();
        if (player == null) return;
        double xpMult = player.getEntityAttribute(ALObjects.Attributes.EXPERIENCE_GAINED.get())
                .getAttributeValue();
        e.setDroppedExperience((int) (e.getDroppedExperience() * xpMult));
    }

    /** Handles HEALING_RECEIVED. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void heal(LivingHealEvent e) {
        float factor = (float) e.getEntityLiving()
                .getEntityAttribute(ALObjects.Attributes.HEALING_RECEIVED.get())
                .getAttributeValue();
        e.setAmount(e.getAmount() * factor);
        if (e.getAmount() <= 0) e.setCanceled(true);
    }

    /** Handles ARROW_DAMAGE and ARROW_VELOCITY. */
    @SubscribeEvent
    public void arrow(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) e.getEntity();
            if (arrow.world.isRemote
                    || arrow.getEntityData().getBoolean("attributeslib.arrow.done")) return;
            if (arrow.shootingEntity instanceof EntityLivingBase) {
                EntityLivingBase le = (EntityLivingBase) arrow.shootingEntity;
                double dmgMult = le.getEntityAttribute(ALObjects.Attributes.ARROW_DAMAGE.get())
                        .getAttributeValue();
                arrow.setDamage(arrow.getDamage() * dmgMult);

                double velMult = le.getEntityAttribute(ALObjects.Attributes.ARROW_VELOCITY.get())
                        .getAttributeValue();
                arrow.motionX *= velMult;
                arrow.motionY *= velMult;
                arrow.motionZ *= velMult;
            }
            arrow.getEntityData().setBoolean("attributeslib.arrow.done", true);
        }
    }

    /** Copied from MeleeAttackGoal#getAttackReachSqr style (1.12.2 uses width fields). */
    private static double getAttackReachSqr(Entity attacker, EntityLivingBase target) {
        return attacker.width * 2.0F * attacker.width * 2.0F + target.width;
    }

    /** DODGE_CHANCE for melee attacks. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dodge(LivingAttackEvent e) {
        EntityLivingBase target = e.getEntityLiving();
        if (target.world.isRemote) return;
        Entity attacker = e.getSource().getImmediateSource();
        if (attacker instanceof EntityLivingBase) {
            double atkRangeSq;
            if (attacker instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) attacker;
                double reach = p.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
                atkRangeSq = reach * reach;
            } else {
                atkRangeSq = getAttackReachSqr(attacker, target);
            }
            if (attacker.getDistanceSq(target) <= atkRangeSq && isDodging(target)) {
                this.onDodge(target);
                e.setCanceled(true);
            }
        }
    }

    /** DODGE_CHANCE for projectiles. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dodge(ProjectileImpactEvent e) {
        RayTraceResult rtr = e.getRayTraceResult();
        Entity target = (rtr != null && rtr.typeOfHit == RayTraceResult.Type.ENTITY)
                ? rtr.entityHit
                : null;
        if (target instanceof EntityLivingBase) {
            EntityLivingBase lvTarget = (EntityLivingBase) target;
            if (isDodging(lvTarget)) {
                this.onDodge(lvTarget);
                e.setCanceled(true);
            }
        }
    }

    private void onDodge(EntityLivingBase target) {
        target.world.playSound(
                null,
                target.getPosition(),
                ALObjects.Sounds.DODGE.get(),
                SoundCategory.NEUTRAL,
                1,
                0.7F + ThreadLocalRandom.current().nextFloat() * 0.3F);
        if (target.world instanceof WorldServer) {
            WorldServer sl = (WorldServer) target.world;
            double height = target.height;
            double width = target.width;
            sl.spawnParticle(
                    EnumParticleTypes.SMOKE_LARGE,
                    target.posX - width / 4,
                    target.posY,
                    target.posZ - width / 4,
                    6,
                    -width / 4,
                    height / 8,
                    -width / 4,
                    0);
        }
    }

    @SubscribeEvent
    public void trackCooldown(AttackEntityEvent e) {
        EntityPlayer p = e.getEntityPlayer();
        AttributesLib.localAtkStrength = p.getCooledAttackStrength(0.5F);
    }

    @SubscribeEvent
    public void valueChanged(AttributeChangedValueEvent e) {
        if (e.getAttributeInstance().getAttribute() == ALObjects.Attributes.CREATIVE_FLIGHT.get()
                && e.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) e.getEntity();

            boolean changed = false;

            if (((IFlying) player).getAndDestroyFlyingCache()) {
                player.capabilities.isFlying = true;
                changed = true;
            }

            if (e.getNewValue() > 0) {
                player.capabilities.allowFlying = true;
                changed = true;
            } else if (e.getOldValue() > 0 && e.getNewValue() <= 0) {
                player.capabilities.allowFlying = false;
                player.capabilities.isFlying = false;
                changed = true;
            }

            if (changed) player.sendPlayerAbilities();
        }
    }

    /**
     * Applies or removes the creative-flight modifier when a player enters/leaves creative or
     * spectator mode. 1.12.2 has no spectator as a WorldType — the gamemode is on the player.
     */
    public static void applyCreativeFlightModifier(EntityPlayer player, boolean creativeOrSpectator) {
        IAttributeInstance inst =
                player.getEntityAttribute(ALObjects.Attributes.CREATIVE_FLIGHT.get());
        if (creativeOrSpectator) {
            if (inst.getModifier(AttributeHelper.CREATIVE_FLIGHT_UUID) == null) {
                inst.applyModifier(
                        new AttributeModifier(
                                AttributeHelper.CREATIVE_FLIGHT_UUID,
                                "attributeslib:creative_flight",
                                1,
                                0));
            }
        } else {
            inst.removeModifier(AttributeHelper.CREATIVE_FLIGHT_UUID);
        }
    }

    /** Random used for dodge calculations; seeded per target+tick. */
    private static Random dodgeRand = new Random();

    public static int computeDodgeSeed(EntityLivingBase target) {
        int delta = 0x9E3779B9;
        int base = target.ticksExisted + target.getUniqueID().hashCode();
        return base + delta + (base << 6) + (base >> 2);
    }

    public static boolean isDodging(EntityLivingBase target) {
        double chance = target.getEntityAttribute(ALObjects.Attributes.DODGE_CHANCE.get())
                .getAttributeValue();
        dodgeRand.setSeed(computeDodgeSeed(target));
        return dodgeRand.nextFloat() <= chance;
    }
}
