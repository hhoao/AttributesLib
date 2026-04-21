package dev.shadowsoffire.attributeslib.api;

import dev.shadowsoffire.attributeslib.AttributesLib;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeMod;

/**
 * A Formattable Attribute is one which elects to have control over its tooltip representation.<br>
 * This interface also serves as the primary means of displaying attribute modifiers.
 */
public interface IFormattableAttribute {

    /**
     * Converts the value of an attribute modifier to the value that will be displayed.
     *
     * <p>For multiplication modifiers, this method is responsible for converting the value to
     * percentage form.<br>
     * The only vanilla attribute which performs value formatting is Knockback Resistance.<br>
     *
     * @param op The operation of the modifier. Null if we are just displaying the raw value and not
     *     a modifier.
     * @param value The value of the modifier.
     * @param flag The tooltip flag.
     * @return The component form of the formatted value.
     */
    default IFormattableTextComponent toValueComponent(
            @Nullable AttributeModifier.Operation op, double value, ITooltipFlag flag) {
        // Knockback Resistance and Swim Speed are percent-based attributes, but we can't registry
        // replace attributes, so we do this here.
        // For Knockback Resistance, vanilla hardcodes a multiplier of 10 for addition values to
        // hide numbers lower than 1,
        // but percent-based is the real desire.
        // For Swim Speed, the implementation is percent-based, but no additional tricks are
        // performed.
        if (this == Attributes.KNOCKBACK_RESISTANCE || this == ForgeMod.SWIM_SPEED.get()) {
            return new TranslationTextComponent(
                    "attributeslib.value.percent", ItemStack.DECIMALFORMAT.format(value * 100));
        }
        // Speed has no metric, so displaying everything as percent works better for the user.
        // However, Speed also operates in that the default is 0.1, not 1, so we have to
        //        // special-case it instead of including it above.
        if (this == Attributes.MOVEMENT_SPEED && isNullOrAddition(op)) {
            return new TranslationTextComponent(
                    "attributeslib.value.percent", ItemStack.DECIMALFORMAT.format(value * 1000));
        }
        String key =
                isNullOrAddition(op) ? "attributeslib.value.flat" : "attributeslib.value.percent";
        return new TranslationTextComponent(
                key, ItemStack.DECIMALFORMAT.format(isNullOrAddition(op) ? value : value * 100));
    }

    /**
     * Converts an attribute modifier into its tooltip representation.
     *
     * <p>This method does not handle formatting of "base" modifiers, such as Attack Damage or
     * Attack Speed.
     *
     * <p>
     *
     * @param modif The attribute modifier being converted to a component.
     * @param flag The tooltip flag.
     * @return The component representation of the passed attribute modifier, with debug info
     *     appended if enabled.
     */
    default IFormattableTextComponent toComponent(AttributeModifier modif, ITooltipFlag flag) {
        Attribute attr = this.ths();
        double value = modif.getAmount();

        IFormattableTextComponent comp;

        if (value > 0.0D) {
            comp =
                    new TranslationTextComponent(
                                    "attributeslib.modifier.plus",
                                    this.toValueComponent(modif.getOperation(), value, flag),
                                    new TranslationTextComponent(attr.getAttributeName()))
                            .mergeStyle(TextFormatting.BLUE);
        } else {
            value *= -1.0D;
            comp =
                    new TranslationTextComponent(
                                    "attributeslib.modifier.take",
                                    this.toValueComponent(modif.getOperation(), value, flag),
                                    new TranslationTextComponent(attr.getAttributeName()))
                            .mergeStyle(TextFormatting.RED);
        }

        return comp.append(this.getDebugInfo(modif, flag));
    }

    /**
     * Computes the additional debug information for a given attribute modifier, if the flag
     * {@linkplain ITooltipFlag#isAdvanced() is advanced}.
     *
     * @param modif The attribute modifier being converted to a component.
     * @param flag The tooltip flag.
     * @return The debug component, or {@link CommonComponents#CommonComponents()} CommonComponents}
     *     if disabled.
     * @apiNote This information is automatically appended to {@link #toComponent(AttributeModifier,
     *     ITooltipFlag)}.
     */
    default ITextComponent getDebugInfo(AttributeModifier modif, ITooltipFlag flag) {
        ITextComponent debugInfo = new StringTextComponent("");

        if (flag.isAdvanced()) {
            // Advanced Tooltips show the underlying operation and the "true" value. We offset
            // MULTIPLY_TOTAL by 1 due to how the operation is calculated.
            double advValue =
                    (modif.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL ? 1 : 0)
                            + modif.getAmount();
            String valueStr = ItemStack.DECIMALFORMAT.format(advValue);
            String txt = "";
            switch (modif.getOperation()) {
                case ADDITION:
                    txt =
                            advValue > 0
                                    ? String.format("[+%s]", valueStr)
                                    : String.format("[%s]", valueStr);
                    break;
                case MULTIPLY_BASE:
                    txt =
                            advValue > 0
                                    ? String.format("[+%sx]", valueStr)
                                    : String.format("[%sx]", valueStr);
                    break;
                case MULTIPLY_TOTAL:
                    txt = String.format("[x%s]", valueStr);
            }
            ;
            debugInfo =
                    new StringTextComponent(" ")
                            .append(new StringTextComponent(txt).mergeStyle(TextFormatting.GRAY));
        }
        return debugInfo;
    }

    /**
     * Gets the specific UUID that represents a "base" (green) modifier for this attribute.
     *
     * @return The UUID of the "base" modifier, or null, if no such modifier may exist.
     */
    @Nullable
    default UUID getBaseUUID() {
        if (this == Attributes.ATTACK_DAMAGE) return AttributeHelper.BASE_ATTACK_DAMAGE;
        else if (this == Attributes.ATTACK_SPEED) return AttributeHelper.BASE_ATTACK_SPEED;
        else if (this == ForgeMod.REACH_DISTANCE.get()) return AttributeHelper.BASE_ENTITY_REACH;
        return null;
    }

    /**
     * Converts an attribute modifier into its tooltip representation.
     *
     * <p>This method does not handle formatting of "base" modifiers, such as Attack Damage or
     * Attack Speed.
     *
     * <p>
     *
     * @param merged The attribute modifier being converted to a component.
     * @param flag The tooltip flag.
     * @return The component representation of the passed attribute modifier.
     */
    default IFormattableTextComponent toBaseComponent(
            double value, double entityBase, boolean merged, ITooltipFlag flag) {
        Attribute attr = this.ths();

        ITextComponent debugInfo = new StringTextComponent("");

        if (flag.isAdvanced() && !merged) {
            // Advanced Tooltips cause us to emit the entity's base value and the base value of the
            // item.
            debugInfo =
                    new StringTextComponent(" ")
                            .append(
                                    new TranslationTextComponent(
                                                    AttributesLib.MODID + ".adv.base",
                                                    ItemStack.DECIMALFORMAT.format(entityBase),
                                                    ItemStack.DECIMALFORMAT.format(
                                                            value - entityBase))
                                            .mergeStyle(TextFormatting.GRAY));
        }

        TextComponent comp =
                new TranslationTextComponent(
                        "attribute.modifier.equals.0",
                        ItemStack.DECIMALFORMAT.format(value),
                        new TranslationTextComponent(attr.getAttributeName()));

        return comp.append(debugInfo);
    }

    /**
     * Certain attributes, such as Attack Damage, are increased by an Enchantment that doesn't
     * actually apply an attribute modifier.<br>
     * This method allows for including certain additional variables in the computation of "base"
     * attribute values.
     *
     * @param stack The stack in question.
     * @return Any bonus value to be applied to the attribute's value, after all modifiers have been
     *     applied.
     */
    default double getBonusBaseValue(ItemStack stack) {
        if (this == Attributes.ATTACK_DAMAGE)
            return EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
        return 0;
    }

    /**
     * This method is invoked when {@link #getBonusBaseValue(ItemStack)} returns a value higher than
     * zero.<br>
     * It is responsible for adding tooltip lines that explain where the bonus values from {@link
     * #getBonusBaseValue(ItemStack)} are from.
     *
     * @param stack The stack in question.
     * @param tooltip The tooltip consumer.
     * @param flag The tooltip flag.
     */
    default void addBonusTooltips(
            ItemStack stack, Consumer<IFormattableTextComponent> tooltip, ITooltipFlag flag) {
        if (this == Attributes.ATTACK_DAMAGE) {
            float sharpness =
                    EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
            ITextComponent debugInfo = new StringTextComponent("");
            if (flag.isAdvanced()) {
                // Show the user that this fake modifier is from Sharpness.
                debugInfo =
                        new StringTextComponent(" ")
                                .append(
                                        new TranslationTextComponent(
                                                        AttributesLib.MODID
                                                                + ".adv.sharpness_bonus",
                                                        sharpness)
                                                .mergeStyle(TextFormatting.GRAY));
            }
            IFormattableTextComponent comp =
                    AttributeHelper.list()
                            .append(
                                    new TranslationTextComponent(
                                                    "attribute.modifier.plus.0",
                                                    ItemStack.DECIMALFORMAT.format(sharpness),
                                                    new TranslationTextComponent(
                                                            this.ths().getAttributeName()))
                                            .mergeStyle(TextFormatting.BLUE));
            tooltip.accept(comp.append(debugInfo));
        }
    }

    default Attribute ths() {
        return (Attribute) this;
    }

    /** Helper method to invoke {@link #toComponent(AttributeModifier, ITooltipFlag)}. */
    public static IFormattableTextComponent toComponent(
            Attribute attr, AttributeModifier modif, ITooltipFlag flag) {
        return ((IFormattableAttribute) attr).toComponent(modif, flag);
    }

    /** Helper method to invoke {@link #toValueComponent(Operation, double, ITooltipFlag)}. */
    public static IFormattableTextComponent toValueComponent(
            Attribute attr, AttributeModifier.Operation op, double value, ITooltipFlag flag) {
        return ((IFormattableAttribute) attr).toValueComponent(op, value, flag);
    }

    /** Helper method to invoke {@link #toBaseComponent(double, double, boolean, ITooltipFlag)} */
    public static IFormattableTextComponent toBaseComponent(
            Attribute attr, double value, double entityBase, boolean merged, ITooltipFlag flag) {
        return ((IFormattableAttribute) attr).toBaseComponent(value, entityBase, merged, flag);
    }

    static boolean isNullOrAddition(@javax.annotation.Nullable AttributeModifier.Operation op) {
        return op == null || op == AttributeModifier.Operation.ADDITION;
    }
}
