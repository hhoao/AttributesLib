package dev.shadowsoffire.attributeslib.api;

import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import java.math.BigDecimal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

/** AL-specific combat calculations for armor and protection values. */
public class ALCombatRules {

    /**
     * Damage taken after applying protection points and protection bypass (PROT_PIERCE/PROT_SHRED).
     * Not invoked if the user has no protection points; excess bypass has no effect.
     */
    public static float getDamageAfterProtection(
            EntityLivingBase target, DamageSource src, float amount, float protPoints) {
        if (src.getImmediateSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) src.getImmediateSource();
            float shred = (float) attacker.getEntityAttribute(Attributes.PROT_SHRED.get()).getAttributeValue();
            if (shred > 0.001F) {
                protPoints *= 1 - shred;
            }
            float pierce = (float) attacker.getEntityAttribute(Attributes.PROT_PIERCE.get()).getAttributeValue();
            if (pierce > 0.001F) {
                protPoints -= pierce;
            }
        }

        if (protPoints <= 0) return amount;
        return amount * getProtDamageReduction(protPoints);
    }

    /**
     * Damage reduction factor for {@code protPoints} — 2.5% each, capped at 85%. May be overridden
     * via config.
     */
    public static float getProtDamageReduction(float protPoints) {
        if (ALConfig.getProtExpr().isPresent()) {
            return ALConfig.getProtExpr()
                    .get()
                    .setVariable("protPoints", new BigDecimal(protPoints))
                    .eval()
                    .floatValue();
        }
        return 1 - Math.min(0.025F * protPoints, 0.85F);
    }

    /**
     * Damage taken after applying armor, toughness, and armor bypass (ARMOR_PIERCE/ARMOR_SHRED).
     * Each toughness point reduces bypass effectiveness by 2%, up to 60%. Toughness no longer
     * reduces damage directly.
     */
    public static float getDamageAfterArmor(
            EntityLivingBase target,
            DamageSource src,
            float amount,
            float armor,
            float toughness) {
        if (src.getImmediateSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) src.getImmediateSource();
            float shred = (float) attacker.getEntityAttribute(Attributes.ARMOR_SHRED.get()).getAttributeValue();
            float bypassResist = Math.min(toughness * 0.02F, 0.6F);
            if (shred > 0.001F) {
                shred *= 1 - bypassResist;
                armor *= 1 - shred;
            }
            float pierce = (float) attacker.getEntityAttribute(Attributes.ARMOR_PIERCE.get()).getAttributeValue();
            if (pierce > 0.001F) {
                pierce *= 1 - bypassResist;
                armor -= pierce;
            }
        }

        if (armor <= 0) return amount;
        return amount * getArmorDamageReduction(amount, armor, toughness);
    }

    /**
     * A-value used in the Y = A / (A + X) armor formula. Flat 10 below 20 damage, then ramps. May be
     * overridden via config.
     */
    public static float getAValue(float damage) {
        if (ALConfig.getAValueExpr().isPresent()) {
            return ALConfig.getAValueExpr()
                    .get()
                    .setVariable("damage", new BigDecimal(damage))
                    .eval()
                    .floatValue();
        }
        return damage < 20 ? 10 : 10 + (damage - 20) / 2;
    }

    /**
     * Armor damage reduction factor — {@code A / (A + armor)}. Toughness no longer enters this
     * calculation. May be overridden via config.
     */
    public static float getArmorDamageReduction(float damage, float armor, float toughness) {
        float a = getAValue(damage);
        if (ALConfig.getArmorExpr().isPresent()) {
            return ALConfig.getArmorExpr()
                    .get()
                    .setVariable("a", new BigDecimal(a))
                    .setVariable("damage", new BigDecimal(damage))
                    .setVariable("armor", new BigDecimal(armor))
                    .setVariable("toughness", new BigDecimal(toughness))
                    .eval()
                    .floatValue();
        }
        return a / (a + armor);
    }
}
