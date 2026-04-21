package dev.shadowsoffire.attributeslib.impl;

import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeChangedValueEvent;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.attributeslib.packet.CritParticleMessage;
import dev.shadowsoffire.attributeslib.util.AttributesUtil;
import dev.shadowsoffire.attributeslib.util.IFlying;
import dev.shadowsoffire.placebo.network.PacketDistro;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.TridentItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.AddReloadListenerEvent;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttributeEvents {

    //    @SubscribeEvent
    //    public void fixChangedAttributes(PlayerLoggedInEvent e) {
    //        AttributeMap map = e.getEntityLiving().getAttributes();
    //        map.getInstance(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(0.6);
    //    }

    private boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ShootableItem || stack.getItem() instanceof TridentItem;
    }

    /**
     * This event handler is the implementation for {@link ALObjects.Attributes#DRAW_SPEED}.<br>
     * Each full point of draw speed provides an extra using tick per game tick.<br>
     * Each partial point of draw speed provides an extra using tick periodically.
     */
    @SubscribeEvent
    public void drawSpeed(LivingEntityUseItemEvent.Tick e) {
        if (e.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) e.getEntity();
            double t = player.getAttribute(ALObjects.Attributes.DRAW_SPEED.get()).getValue() - 1;
            if (t == 0 || !this.canBenefitFromDrawSpeed(e.getItem())) return;

            // Handle negative draw speed.
            int offset = -1;
            if (t < 0) {
                offset = 1;
                t = -t;
            }

            while (t > 1) { // Every 100% triggers an immediate extra tick
                e.setDuration(e.getDuration() + offset);
                t--;
            }

            if (t > 0.5F) { // Special case 0.5F so that values in (0.5, 1) don't round to 1.
                if (e.getEntity().ticksExisted % 2 == 0) e.setDuration(e.getDuration() + offset);
                t -= 0.5F;
            }

            int mod = (int) Math.floor(1 / Math.min(1, t));
            if (e.getEntity().ticksExisted % mod == 0) e.setDuration(e.getDuration() + offset);
            t--;
        }
    }

    /** This event handler manages the Life Steal and Overheal attributes. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void lifeStealOverheal(LivingHurtEvent e) {
        if (e.getSource().getImmediateSource() instanceof LivingEntity
                && AttributesUtil.isPhysicalDamage(e.getSource())) {
            LivingEntity attacker = (LivingEntity) e.getSource().getImmediateSource();
            float lifesteal =
                    (float) attacker.getAttributeValue(ALObjects.Attributes.LIFE_STEAL.get());
            float dmg = Math.min(e.getAmount(), e.getEntityLiving().getHealth());
            if (lifesteal > 0.001) {
                attacker.heal(dmg * lifesteal);
            }
            float overheal =
                    (float) attacker.getAttributeValue(ALObjects.Attributes.OVERHEAL.get());
            float maxOverheal = attacker.getMaxHealth() * 0.5F;
            if (overheal > 0 && attacker.getAbsorptionAmount() < maxOverheal) {
                attacker.setAbsorptionAmount(
                        Math.min(maxOverheal, attacker.getAbsorptionAmount() + dmg * overheal));
            }
        }
    }

    /**
     * Recursion guard for {@link #meleeDamageAttributes(LivingAttackEvent)}.<br>
     * Doesn't need to be ThreadLocal as attack logic is main-thread only.
     */
    private static boolean noRecurse = false;

    /**
     * Applies the following melee damage attributes:<br>
     *
     * <ul>
     *   <li>{@link ALObjects.Attributes#CURRENT_HP_DAMAGE}
     *   <li>{@link ALObjects.Attributes#FIRE_DAMAGE}
     *   <li>{@link ALObjects.Attributes#COLD_DAMAGE}
     * </ul>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void meleeDamageAttributes(LivingAttackEvent e) {
        if (e.getEntityLiving().world.isRemote || e.getEntityLiving().getShouldBeDead()) return;
        if (noRecurse) return;
        noRecurse = true;
        if (e.getSource().getImmediateSource() instanceof LivingEntity
                && AttributesUtil.isPhysicalDamage(e.getSource())) {
            LivingEntity attacker = (LivingEntity) e.getSource().getImmediateSource();
            float hpDmg =
                    (float)
                            attacker.getAttributeValue(
                                    ALObjects.Attributes.CURRENT_HP_DAMAGE.get());
            float fireDmg =
                    (float) attacker.getAttributeValue(ALObjects.Attributes.FIRE_DAMAGE.get());
            float coldDmg =
                    (float) attacker.getAttributeValue(ALObjects.Attributes.COLD_DAMAGE.get());
            LivingEntity target = e.getEntityLiving();
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
                target.setFire(target.getFireTimer() + (int) (10 * fireDmg));
            }
            target.hurtResistantTime = 0;
            if (coldDmg > 0.001 && AttributesLib.localAtkStrength >= 0.55F) {
                target.attackEntityFrom(
                        src(ALObjects.DamageTypes.COLD_DAMAGE, attacker),
                        AttributesLib.localAtkStrength * coldDmg);
                target.addPotionEffect(
                        new EffectInstance(
                                Effects.SLOWNESS,
                                (int) (15 * coldDmg),
                                MathHelper.floor(coldDmg / 5)));
            }
            target.hurtResistantTime = time;
            if (target.getShouldBeDead()) {
                target.getPersistentData().putBoolean("apoth.killed_by_aux_dmg", true);
            }
        }
        noRecurse = false;
    }

    public static DamageSource src(DamageSource type, LivingEntity entity) {
        return new EntityDamageSource(type.damageType, entity);
    }

    /**
     * Handles {@link ALObjects.Attributes#CRIT_CHANCE} and {@link ALObjects.Attributes#CRIT_DAMAGE}
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void apothCriticalStrike(LivingHurtEvent e) {
        LivingEntity attacker =
                e.getSource().getImmediateSource() instanceof LivingEntity
                        ? (LivingEntity) e.getSource().getImmediateSource()
                        : null;
        if (attacker == null) return;

        double critChance = attacker.getAttributeValue(ALObjects.Attributes.CRIT_CHANCE.get());
        float critDmg = (float) attacker.getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE.get());

        ThreadLocalRandom current = ThreadLocalRandom.current();

        float critMult = 1.0F;

        // Roll for crits. Each overcrit reduces the effectiveness by 15%
        // We stop rolling when crit chance fails or the crit damage would reduce the total damage
        // dealt.
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
                    (ServerWorld) attacker.world,
                    e.getEntity().getPosition());
        }
    }

    /**
     * Handles {@link ALObjects.Attributes#CRIT_DAMAGE}'s interactions with vanilla critical
     * strikes.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void vanillaCritDmg(CriticalHitEvent e) {
        float critDmg =
                (float)
                        e.getEntityLiving()
                                .getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE.get());
        if (e.isVanillaCritical()) {
            e.setDamageModifier(Math.max(e.getDamageModifier(), critDmg));
        }
    }

    /** Handles {@link ALObjects.Attributes#MINING_SPEED} */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void breakSpd(BreakSpeed e) {
        e.setNewSpeed(
                e.getNewSpeed()
                        * (float)
                                e.getEntityLiving()
                                        .getAttributeValue(
                                                ALObjects.Attributes.MINING_SPEED.get()));
    }

    /**
     * This event, and {@linkplain #mobXp(LivingExperienceDropEvent) the event below} handle {@link
     * ALObjects.Attributes#EXPERIENCE_GAINED}
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void blockBreak(BlockEvent.BreakEvent e) {
        double xpMult =
                e.getPlayer().getAttributeValue(ALObjects.Attributes.EXPERIENCE_GAINED.get());
        e.setExpToDrop((int) (e.getExpToDrop() * xpMult));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void mobXp(LivingExperienceDropEvent e) {
        PlayerEntity player = e.getAttackingPlayer();
        if (player == null) return;
        double xpMult =
                e.getAttackingPlayer()
                        .getAttributeValue(ALObjects.Attributes.EXPERIENCE_GAINED.get());
        e.setDroppedExperience((int) (e.getDroppedExperience() * xpMult));
    }

    /** Handles {@link ALObjects.Attributes#HEALING_RECEIVED} */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void heal(LivingHealEvent e) {
        float factor =
                (float)
                        e.getEntityLiving()
                                .getAttributeValue(ALObjects.Attributes.HEALING_RECEIVED.get());
        e.setAmount(e.getAmount() * factor);
        if (e.getAmount() <= 0) e.setCanceled(true);
    }

    /**
     * Handles {@link ALObjects.Attributes#ARROW_DAMAGE} and {@link
     * ALObjects.Attributes#ARROW_VELOCITY}
     */
    @SubscribeEvent
    public void arrow(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof AbstractArrowEntity) {
            AbstractArrowEntity arrow = (AbstractArrowEntity) e.getEntity();
            if (arrow.world.isRemote
                    || arrow.getPersistentData().getBoolean("attributeslib.arrow.done")) return;
            if (arrow.func_234616_v_() instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) arrow.func_234616_v_();
                arrow.setDamage(
                        arrow.getDamage()
                                * le.getAttributeValue(ALObjects.Attributes.ARROW_DAMAGE.get()));
                arrow.setMotion(
                        arrow.getMotion()
                                .scale(
                                        le.getAttributeValue(
                                                ALObjects.Attributes.ARROW_VELOCITY.get())));
            }
            arrow.getPersistentData().putBoolean("attributeslib.arrow.done", true);
        }
    }

    /** Copied from {@link MeleeAttackGoal#getAttackReachSqr} */
    private static double getAttackReachSqr(Entity attacker, LivingEntity pAttackTarget) {
        return attacker.getWidth() * 2.0F * attacker.getWidth() * 2.0F + pAttackTarget.getWidth();
    }

    /** Handles {@link ALObjects.Attributes#DODGE_CHANCE} for melee attacks. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dodge(LivingAttackEvent e) {
        LivingEntity target = e.getEntityLiving();
        if (target.world.isRemote) return;
        Entity attacker = e.getSource().getImmediateSource();
        if (attacker instanceof LivingEntity) {
            double atkRangeSq;
            if (attacker instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity) attacker;
                atkRangeSq =
                        p.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue()
                                * p.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
            } else {
                atkRangeSq = getAttackReachSqr(attacker, target);
            }
            if (attacker.getDistanceSq(target) <= atkRangeSq && isDodging(target)) {
                this.onDodge(target);
                e.setCanceled(true);
            }
        }
    }

    /** Handles {@link ALObjects.Attributes#DODGE_CHANCE} for projectiles. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dodge(ProjectileImpactEvent e) {
        Entity target =
                e.getRayTraceResult() instanceof EntityRayTraceResult
                        ? ((EntityRayTraceResult) e.getRayTraceResult()).getEntity()
                        : null;
        if (target instanceof LivingEntity) {
            LivingEntity lvTarget = (LivingEntity) target;
            // We can skip the distance check for projectiles, as "Projectile Impact" means the
            // projectile is on the target.
            if (isDodging(lvTarget)) {
                this.onDodge(lvTarget);
                e.setCanceled(true);
            }
        }
    }

    private void onDodge(LivingEntity target) {
        target.world.playSound(
                null,
                target.getPosition(),
                ALObjects.Sounds.DODGE.get(),
                SoundCategory.NEUTRAL,
                1,
                0.7F + ThreadLocalRandom.current().nextFloat() * 0.3F);
        if (target.world instanceof ServerWorld) {
            ServerWorld sl = (ServerWorld) target.world;
            double height = target.getHeight();
            double width = target.getWidth();
            sl.spawnParticle(
                    ParticleTypes.LARGE_SMOKE,
                    target.getPosX() - width / 4,
                    target.getPosY(),
                    target.getPosZ() - width / 4,
                    6,
                    -width / 4,
                    height / 8,
                    -width / 4,
                    0);
        }
    }

    /** Fix for https://github.com/MinecraftForge/MinecraftForge/issues/9370 */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void fixMCF9370(ProjectileImpactEvent e) {
        if (e.isCanceled()) {
            Entity target =
                    e.getRayTraceResult() instanceof EntityRayTraceResult
                            ? ((EntityRayTraceResult) e.getRayTraceResult()).getEntity()
                            : null;
            ProjectileEntity proj = (ProjectileEntity) e.getEntity();
            if (proj instanceof AbstractArrowEntity) {
                AbstractArrowEntity arrow = (AbstractArrowEntity) proj;
                if (target != null
                        && proj instanceof AbstractArrowEntity
                        && arrow.getPierceLevel() > 0) {
                    if (arrow.piercedEntities == null) {
                        arrow.piercedEntities = new IntOpenHashSet(arrow.getPierceLevel());
                    }
                    arrow.piercedEntities.add(target.getEntityId());
                }
            }
        }
    }

    @SubscribeEvent
    public void trackCooldown(AttackEntityEvent e) {
        PlayerEntity p = e.getPlayer();

        AttributesLib.localAtkStrength = p.getCooledAttackStrength(0.5F);
    }

    @SubscribeEvent
    public void valueChanged(AttributeChangedValueEvent e) {
        // AttributesLib.LOGGER.info("Attribute {} changed value from {} to {}!",
        // e.getAttributeInstance().getAttribute().getDescriptionId(), e.getOldValue(),
        // e.getNewValue());
        if (e.getAttributeInstance().getAttribute() == ALObjects.Attributes.CREATIVE_FLIGHT.get()
                && e.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) e.getEntity();

            boolean changed = false;

            if (((IFlying) player).getAndDestroyFlyingCache()) {
                player.abilities.isFlying = true;
                changed = true;
            }

            if (e.getNewValue() > 0) {
                player.abilities.allowFlying = true;
                changed = true;
            } else if (e.getOldValue() > 0 && e.getNewValue() <= 0) {
                player.abilities.allowFlying = false;
                player.abilities.isFlying = false;
                changed = true;
            }

            if (changed) player.sendPlayerAbilities();
        }
    }

    public static void applyCreativeFlightModifier(PlayerEntity player, GameType newType) {
        ModifiableAttributeInstance inst =
                player.getAttribute(ALObjects.Attributes.CREATIVE_FLIGHT.get());
        if (newType == GameType.CREATIVE || newType == GameType.SPECTATOR) {
            if (inst.getModifier(AttributeHelper.CREATIVE_FLIGHT_UUID) == null) {
                inst.applyPersistentModifier(
                        new AttributeModifier(
                                AttributeHelper.CREATIVE_FLIGHT_UUID,
                                () -> "attributeslib:creative_flight",
                                1,
                                AttributeModifier.Operation.ADDITION));
            }
        } else {
            inst.removeModifier(AttributeHelper.CREATIVE_FLIGHT_UUID);
        }
    }

    @SubscribeEvent
    public void reloads(AddReloadListenerEvent e) {
        e.addListener(ALConfig.makeReloader());
    }

    /**
     * Random used for dodge calculations.<br>
     * This random is seeded with the target entity's tick count before use.
     */
    private static Random dodgeRand = new Random();

    /**
     * Computes the dodge random seed for the entity. This seed is only unique for the current tick,
     * so that multiple damage instances in the same tick are all dodged.
     *
     * <p>Without this, it would be possible for multiple-instances attacks to only be partially
     * dodged.
     *
     * @param target The entity being attecked who is rolling to dodge.
     * @return The random seed to use when computing the dodge roll
     */
    public static int computeDodgeSeed(LivingEntity target) {
        int delta = 0x9E3779B9;
        int base = target.ticksExisted + target.getUniqueID().hashCode();
        return base + delta + (base << 6) + (base >> 2);
    }

    /**
     * Checks if the target entity will dodge attacks in the current tick, by checking the {@link
     * ALObjects.Attributes#DODGE_CHANCE} value and rolling a random.
     *
     * @param target The entity being attecked who is rolling to dodge.
     * @return True if the target may dodge, false otherwise.
     */
    public static boolean isDodging(LivingEntity target) {
        double chance = target.getAttributeValue(ALObjects.Attributes.DODGE_CHANCE.get());
        dodgeRand.setSeed(computeDodgeSeed(target));
        return dodgeRand.nextFloat() <= chance;
    }
}
