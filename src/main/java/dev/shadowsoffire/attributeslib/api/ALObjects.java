package dev.shadowsoffire.attributeslib.api;

import static dev.shadowsoffire.attributeslib.AttributesLib.R;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.impl.BooleanAttribute;
import dev.shadowsoffire.attributeslib.impl.PercentBasedAttribute;
import dev.shadowsoffire.attributeslib.mobfx.BleedingEffect;
import dev.shadowsoffire.attributeslib.mobfx.DetonationEffect;
import dev.shadowsoffire.attributeslib.mobfx.FlyingEffect;
import dev.shadowsoffire.attributeslib.mobfx.GrievousEffect;
import dev.shadowsoffire.attributeslib.mobfx.KnowledgeEffect;
import dev.shadowsoffire.attributeslib.mobfx.SunderingEffect;
import dev.shadowsoffire.attributeslib.mobfx.VitalityEffect;
import dev.shadowsoffire.placebo.config.DeferredHelper.RegObj;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ALObjects {

    /**
     * 1.12.2 attribute registration. Since {@code IAttribute} is not a forge registry entry, we keep
     * static references directly. An {@code EntityLivingBase} mixin registers each attribute onto
     * every living entity's attribute map at construction time.
     */
    public static class Attributes {

        /** Flat armor penetration. Base value = (0.0) = 0 armor reduced during damage calculations. */
        public static final RegObj<IAttribute> ARMOR_PIERCE =
                R.attribute(
                        "armor_pierce",
                        () -> ranged("attributeslib.armor_pierce", 0.0D, 0.0D, 1000.0D));

        /** Percentage armor reduction. Base value = (0.0) = 0% of armor reduced. */
        public static final RegObj<IAttribute> ARMOR_SHRED =
                R.attribute(
                        "armor_shred",
                        () -> percent("attributeslib.armor_shred", 0.0D, 0.0D, 2.0D));

        /** Arrow Damage. Base value = (1.0) = 100% default arrow damage. */
        public static final RegObj<IAttribute> ARROW_DAMAGE =
                R.attribute(
                        "arrow_damage",
                        () -> percent("attributeslib.arrow_damage", 1.0D, 0.0D, 10.0D));

        /** Arrow Velocity. Base value = (1.0) = 100% default arrow velocity. */
        public static final RegObj<IAttribute> ARROW_VELOCITY =
                R.attribute(
                        "arrow_velocity",
                        () -> percent("attributeslib.arrow_velocity", 1.0D, 0.0D, 10.0D));

        /** Bonus magic damage that slows enemies hit. Base value = (0.0) = 0 damage. */
        public static final RegObj<IAttribute> COLD_DAMAGE =
                R.attribute(
                        "cold_damage",
                        () -> ranged("attributeslib.cold_damage", 0.0D, 0.0D, 1000.0D));

        /** Chance that any attack will critically strike. Base value = (0.05) = 5%. */
        public static final RegObj<IAttribute> CRIT_CHANCE =
                R.attribute(
                        "crit_chance",
                        () -> percent("attributeslib.crit_chance", 0.05D, 0.0D, 10.0D));

        /** Amount of damage caused by critical strikes. Base value = (1.5) = 150%. */
        public static final RegObj<IAttribute> CRIT_DAMAGE =
                R.attribute(
                        "crit_damage",
                        () -> percent("attributeslib.crit_damage", 1.5D, 1.0D, 100.0D));

        /** Bonus physical damage dealt equal to enemy's current health. */
        public static final RegObj<IAttribute> CURRENT_HP_DAMAGE =
                R.attribute(
                        "current_hp_damage",
                        () -> percent("attributeslib.current_hp_damage", 0.0D, 0.0D, 1.0D));

        /** Chance to dodge incoming melee damage. */
        public static final RegObj<IAttribute> DODGE_CHANCE =
                R.attribute(
                        "dodge_chance",
                        () -> percent("attributeslib.dodge_chance", 0.0D, 0.0D, 1.0D));

        /** How fast a ranged weapon is charged. */
        public static final RegObj<IAttribute> DRAW_SPEED =
                R.attribute(
                        "draw_speed",
                        () -> percent("attributeslib.draw_speed", 1.0D, 0.0D, 4.0D));

        /** Experience multiplier from killing mobs or breaking ores. */
        public static final RegObj<IAttribute> EXPERIENCE_GAINED =
                R.attribute(
                        "experience_gained",
                        () -> percent("attributeslib.experience_gained", 1.0D, 0.0D, 1000.0D));

        /** Bonus magic damage that burns enemies hit. */
        public static final RegObj<IAttribute> FIRE_DAMAGE =
                R.attribute(
                        "fire_damage",
                        () -> ranged("attributeslib.fire_damage", 0.0D, 0.0D, 1000.0D));

        /** Extra health that regenerates when not taking damage. */
        public static final RegObj<IAttribute> GHOST_HEALTH =
                R.attribute(
                        "ghost_health",
                        () -> ranged("attributeslib.ghost_health", 0.0D, 0.0D, 1000.0D));

        /** Adjusts all healing received. */
        public static final RegObj<IAttribute> HEALING_RECEIVED =
                R.attribute(
                        "healing_received",
                        () -> percent("attributeslib.healing_received", 1.0D, 0.0D, 1000.0D));

        /** Percent of physical damage converted to health. */
        public static final RegObj<IAttribute> LIFE_STEAL =
                R.attribute(
                        "life_steal",
                        () -> percent("attributeslib.life_steal", 0.0D, 0.0D, 10.0D));

        /** Mining Speed. */
        public static final RegObj<IAttribute> MINING_SPEED =
                R.attribute(
                        "mining_speed",
                        () -> percent("attributeslib.mining_speed", 1.0D, 0.0D, 10.0D));

        /** Percent of physical damage converted to absorption hearts. */
        public static final RegObj<IAttribute> OVERHEAL =
                R.attribute(
                        "overheal",
                        () -> percent("attributeslib.overheal", 0.0D, 0.0D, 10.0D));

        /** Flat protection penetration. */
        public static final RegObj<IAttribute> PROT_PIERCE =
                R.attribute(
                        "prot_pierce",
                        () -> ranged("attributeslib.prot_pierce", 0.0D, 0.0D, 34.0D));

        /** Percentage protection reduction. */
        public static final RegObj<IAttribute> PROT_SHRED =
                R.attribute(
                        "prot_shred",
                        () -> percent("attributeslib.prot_shred", 0.0D, 0.0D, 1.0D));

        /** Boolean attribute for if elytra flight is enabled. */
        public static final RegObj<IAttribute> ELYTRA_FLIGHT =
                R.attribute(
                        "elytra_flight",
                        () -> bool("attributeslib.elytra_flight", false));

        /** Boolean attribute for if creative flight is enabled. */
        public static final RegObj<IAttribute> CREATIVE_FLIGHT =
                R.attribute(
                        "creative_flight",
                        () -> bool("attributeslib.creative_flight", false));

        private static IAttribute ranged(String name, double def, double min, double max) {
            RangedAttribute attr = new RangedAttribute(null, name, def, min, max);
            attr.setShouldWatch(true);
            return attr;
        }

        private static IAttribute percent(String name, double def, double min, double max) {
            PercentBasedAttribute attr = new PercentBasedAttribute(null, name, def, min, max);
            attr.setShouldWatch(true);
            return attr;
        }

        private static IAttribute bool(String name, boolean def) {
            BooleanAttribute attr = new BooleanAttribute(null, name, def);
            attr.setShouldWatch(true);
            return attr;
        }

        public static void bootstrap() {}
    }

    public static class MobEffects {

        public static final RegObj<BleedingEffect> BLEEDING =
                R.effect("bleeding", BleedingEffect::new);

        public static final RegObj<DetonationEffect> DETONATION =
                R.effect("detonation", DetonationEffect::new);

        public static final RegObj<GrievousEffect> GRIEVOUS =
                R.effect("grievous", GrievousEffect::new);

        public static final RegObj<KnowledgeEffect> KNOWLEDGE =
                R.effect("knowledge", KnowledgeEffect::new);

        public static final RegObj<SunderingEffect> SUNDERING =
                R.effect("sundering", SunderingEffect::new);

        public static final RegObj<VitalityEffect> VITALITY =
                R.effect("vitality", VitalityEffect::new);

        public static final RegObj<FlyingEffect> FLYING =
                R.effect("flying", FlyingEffect::new);

        public static void bootstrap() {}
    }

    /**
     * 1.12.2 has no particle registry. The custom {@code apoth_crit} particle is spawned directly by
     * {@link dev.shadowsoffire.attributeslib.packet.CritParticleMessage} using a custom {@code
     * Particle} subclass registered on the client. This constant is kept as a discriminator for the
     * packet payload.
     */
    public static class Particles {

        /** Discriminator for the apoth_crit particle in {@code CritParticleMessage}. */
        public static final int APOTH_CRIT = 0;

        public static void bootstrap() {}
    }

    public static class Sounds {

        public static final RegObj<SoundEvent> DODGE = R.sound("dodge");

        public static void bootstrap() {}
    }

    public static class Tags {

        /**
         * An attribute with a dynamic base cannot have its value computed out of context, and is
         * instead treated as a list of modifiers that will be applied when the event occurs. The
         * applied modifiers will use the normal rules of operations but on the dynamic base.
         */
        public static final ResourceLocation DYNAMIC_BASE_ATTTE = AttributesLib.loc("dynamic_base");
    }

    public static class DamageTypes {

        /** Damage type used by {@link MobEffects#BLEEDING}. Bypasses armor. */
        public static final DamageSource BLEEDING =
                new DamageSource("bleeding").setDamageBypassesArmor().setMagicDamage();

        /**
         * Damage type used by {@link MobEffects#DETONATION}. Bypasses armor, and is marked as magic
         * damage.
         */
        public static final DamageSource DETONATION = new DamageSource("detonation");

        /**
         * Damage type used by {@link Attributes#CURRENT_HP_DAMAGE}. Same properties as generic
         * physical damage. Has attacker context.
         */
        public static final DamageSource CURRENT_HP_DAMAGE = new DamageSource("current_hp_damage");

        /**
         * Damage type used by {@link Attributes#FIRE_DAMAGE}. Bypasses armor, and is marked as
         * magic damage. Has attacker context.<br>
         * Not marked as fire damage until fire resistance is reworked to not block all fire damage.
         */
        public static final DamageSource FIRE_DAMAGE = new DamageSource("fire_damage");

        /**
         * Damage type used by {@link Attributes#COLD_DAMAGE}. Bypasses armor, and is marked as
         * magic damage. Has attacker context.
         */
        public static final DamageSource COLD_DAMAGE = new DamageSource("cold_damage");

        public static void bootstrap() {}
    }

    public static void bootstrap() {
        Attributes.bootstrap();
        MobEffects.bootstrap();
        Particles.bootstrap();
        Sounds.bootstrap();
        DamageTypes.bootstrap();
    }
}
