package dev.shadowsoffire.attributeslib.api;

import dev.shadowsoffire.attributeslib.AttributesLib;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * A Formattable Attribute elects to control its tooltip representation. Also serves as the primary
 * means of displaying attribute modifiers. In 1.12.2 this is applied onto {@link IAttribute}
 * instances via a mixin on {@code BaseAttribute}.
 *
 * <p>{@link AttributeModifier#getOperation()} returns an {@code int} in 1.12.2 ({@code 0} =
 * addition, {@code 1} = multiply base, {@code 2} = multiply total), so operation parameters here are
 * typed as {@link Integer} to permit {@code null} for "no modifier / base value" callers.
 */
public interface IFormattableAttribute {

    /** Additive operation. Equivalent to {@code AttributeModifier.Operation.ADDITION} in 1.16+. */
    int OP_ADDITION = 0;

    /** Multiplicative operation against the base value. Equivalent to {@code MULTIPLY_BASE}. */
    int OP_MULTIPLY_BASE = 1;

    /** Multiplicative operation against the running total. Equivalent to {@code MULTIPLY_TOTAL}. */
    int OP_MULTIPLY_TOTAL = 2;

    /**
     * Converts the value of an attribute modifier to its displayable form. Multiplication operations
     * are converted to percent form here.
     */
    default ITextComponent toValueComponent(
            @Nullable Integer op, double value, ITooltipFlag flag) {
        IAttribute self = this.ths();
        if (self == SharedMonsterAttributes.KNOCKBACK_RESISTANCE) {
            return new TextComponentTranslation(
                    "attributeslib.value.percent", ItemStack.DECIMALFORMAT.format(value * 100));
        }
        if (self == SharedMonsterAttributes.MOVEMENT_SPEED && isNullOrAddition(op)) {
            return new TextComponentTranslation(
                    "attributeslib.value.percent", ItemStack.DECIMALFORMAT.format(value * 1000));
        }
        String key =
                isNullOrAddition(op) ? "attributeslib.value.flat" : "attributeslib.value.percent";
        return new TextComponentTranslation(
                key, ItemStack.DECIMALFORMAT.format(isNullOrAddition(op) ? value : value * 100));
    }

    /**
     * Converts an attribute modifier into its tooltip representation. Does not handle "base"
     * modifiers (Attack Damage, Attack Speed). Debug info is appended when advanced tooltips are on.
     */
    default ITextComponent toComponent(AttributeModifier modif, ITooltipFlag flag) {
        IAttribute attr = this.ths();
        double value = modif.getAmount();

        TextFormatting color = value > 0.0D ? TextFormatting.BLUE : TextFormatting.RED;
        String key = value > 0.0D ? "attributeslib.modifier.plus" : "attributeslib.modifier.take";
        if (value < 0.0D) value *= -1.0D;

        ITextComponent comp =
                new TextComponentTranslation(
                                key,
                                this.toValueComponent(modif.getOperation(), value, flag),
                                new TextComponentTranslation(attr.getName()))
                        .setStyle(new Style().setColor(color));

        ITextComponent debug = this.getDebugInfo(modif, flag);
        if (debug != null) comp.appendSibling(debug);
        return comp;
    }

    /** Advanced-only debug info appended to modifier tooltips: operation + true value. */
    default ITextComponent getDebugInfo(AttributeModifier modif, ITooltipFlag flag) {
        if (!flag.isAdvanced()) return new TextComponentString("");

        int op = modif.getOperation();
        double advValue = (op == OP_MULTIPLY_TOTAL ? 1 : 0) + modif.getAmount();
        String valueStr = ItemStack.DECIMALFORMAT.format(advValue);
        String txt;
        switch (op) {
            case OP_ADDITION:
                txt = advValue > 0 ? String.format("[+%s]", valueStr) : String.format("[%s]", valueStr);
                break;
            case OP_MULTIPLY_BASE:
                txt =
                        advValue > 0
                                ? String.format("[+%sx]", valueStr)
                                : String.format("[%sx]", valueStr);
                break;
            case OP_MULTIPLY_TOTAL:
                txt = String.format("[x%s]", valueStr);
                break;
            default:
                txt = "";
        }
        return new TextComponentString(" ")
                .appendSibling(
                        new TextComponentString(txt).setStyle(new Style().setColor(TextFormatting.GRAY)));
    }

    /** UUID of the "base" (green) modifier for this attribute, or null if none. */
    @Nullable
    default UUID getBaseUUID() {
        if (this == SharedMonsterAttributes.ATTACK_DAMAGE) return AttributeHelper.BASE_ATTACK_DAMAGE;
        if (this == SharedMonsterAttributes.ATTACK_SPEED) return AttributeHelper.BASE_ATTACK_SPEED;
        if ("generic.reachDistance".equals(this.ths().getName())) return AttributeHelper.BASE_ENTITY_REACH;
        return null;
    }

    /**
     * Renders the "base" (green) tooltip line for attributes like attack damage. When advanced
     * tooltips are active, debug info with the item-supplied portion is appended.
     */
    default ITextComponent toBaseComponent(
            double value, double entityBase, boolean merged, ITooltipFlag flag) {
        IAttribute attr = this.ths();

        ITextComponent comp =
                new TextComponentTranslation(
                        "attribute.modifier.equals.0",
                        ItemStack.DECIMALFORMAT.format(value),
                        new TextComponentTranslation(attr.getName()));

        if (flag.isAdvanced() && !merged) {
            ITextComponent debug =
                    new TextComponentString(" ")
                            .appendSibling(
                                    new TextComponentTranslation(
                                                    AttributesLib.MODID + ".adv.base",
                                                    ItemStack.DECIMALFORMAT.format(entityBase),
                                                    ItemStack.DECIMALFORMAT.format(value - entityBase))
                                            .setStyle(new Style().setColor(TextFormatting.GRAY)));
            comp.appendSibling(debug);
        }

        return comp;
    }

    /**
     * Certain attributes (e.g. Attack Damage) are boosted by enchantments that don't apply modifiers.
     * This method returns the additional bonus to fold into the "base" value display.
     */
    default double getBonusBaseValue(ItemStack stack) {
        if (this == SharedMonsterAttributes.ATTACK_DAMAGE) {
            return EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
        }
        return 0;
    }

    /**
     * Adds tooltip lines explaining where bonus values from {@link #getBonusBaseValue(ItemStack)}
     * come from.
     */
    default void addBonusTooltips(
            ItemStack stack, Consumer<ITextComponent> tooltip, ITooltipFlag flag) {
        if (this == SharedMonsterAttributes.ATTACK_DAMAGE) {
            float sharpness =
                    EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
            ITextComponent comp =
                    AttributeHelper.list()
                            .appendSibling(
                                    new TextComponentTranslation(
                                                    "attribute.modifier.plus.0",
                                                    ItemStack.DECIMALFORMAT.format(sharpness),
                                                    new TextComponentTranslation(this.ths().getName()))
                                            .setStyle(new Style().setColor(TextFormatting.BLUE)));
            if (flag.isAdvanced()) {
                comp.appendSibling(
                        new TextComponentString(" ")
                                .appendSibling(
                                        new TextComponentTranslation(
                                                        AttributesLib.MODID + ".adv.sharpness_bonus",
                                                        sharpness)
                                                .setStyle(new Style().setColor(TextFormatting.GRAY))));
            }
            tooltip.accept(comp);
        }
    }

    default IAttribute ths() {
        return (IAttribute) this;
    }

    public static ITextComponent toComponent(
            IAttribute attr, AttributeModifier modif, ITooltipFlag flag) {
        return ((IFormattableAttribute) attr).toComponent(modif, flag);
    }

    public static ITextComponent toValueComponent(
            IAttribute attr, @Nullable Integer op, double value, ITooltipFlag flag) {
        return ((IFormattableAttribute) attr).toValueComponent(op, value, flag);
    }

    public static ITextComponent toBaseComponent(
            IAttribute attr, double value, double entityBase, boolean merged, ITooltipFlag flag) {
        return ((IFormattableAttribute) attr).toBaseComponent(value, entityBase, merged, flag);
    }

    static boolean isNullOrAddition(@Nullable Integer op) {
        return op == null || op == OP_ADDITION;
    }
}
