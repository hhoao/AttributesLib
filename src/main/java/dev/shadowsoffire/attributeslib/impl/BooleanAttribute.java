package dev.shadowsoffire.attributeslib.impl;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A Boolean Attribute is one which displays modifiers as "Enables" or "Forcibly Disables".<br>
 * For these attributes, you should only use the following modifier values:
 *
 * <ul>
 *   <li>A value of 1 with {@link Operation#ADDITION} to enable the effect.
 *   <li>A value of -1 with {@link Operation#MULTIPLY_TOTAL} to forcibly disable the effect.
 * </ul>
 *
 * This behavior allows for multiple enables to coexist, not removing the effect unless all enabling
 * modifiers are removed.<br>
 * Additionally, it permits forcibly disabling the attribute through multiply total.
 *
 * <p>Modifiers not using one of the specified modifiers noted above will display as an error
 * condition.
 */
public class BooleanAttribute extends Attribute implements IFormattableAttribute {

    public BooleanAttribute(String pDescriptionId, boolean defaultValue) {
        super(pDescriptionId, defaultValue ? 1 : 0);
    }

    @Override
    public IFormattableTextComponent toValueComponent(
            AttributeModifier.Operation op, double value, ITooltipFlag flag) {
        if (op == null) {
            return new TranslationTextComponent(
                    "attributeslib.value.boolean." + (value > 0 ? "enabled" : "disabled"));
        } else if (op == AttributeModifier.Operation.ADDITION && (int) value == 1) {
            return new TranslationTextComponent("attributeslib.value.boolean.enable");
        } else if (op == AttributeModifier.Operation.MULTIPLY_TOTAL && (int) value == -1) {
            return new TranslationTextComponent("attributeslib.value.boolean.force_disable");
        } else return new TranslationTextComponent("attributeslib.value.boolean.invalid");
    }

    @Override
    public IFormattableTextComponent toComponent(AttributeModifier modif, ITooltipFlag flag) {
        Attribute attr = this.ths();
        double value = modif.getAmount();

        IFormattableTextComponent comp;

        if (value > 0.0D) {
            comp =
                    new TranslationTextComponent(
                                    "attributeslib.modifier.bool",
                                    this.toValueComponent(modif.getOperation(), value, flag),
                                    new TranslationTextComponent(attr.getAttributeName()))
                            .mergeStyle(TextFormatting.BLUE);
        } else {
            value *= -1.0D;
            comp =
                    new TranslationTextComponent(
                                    "attributeslib.modifier.bool",
                                    this.toValueComponent(modif.getOperation(), value, flag),
                                    new TranslationTextComponent(attr.getAttributeName()))
                            .mergeStyle(TextFormatting.RED);
        }

        return comp.append(this.getDebugInfo(modif, flag));
    }

    @Override
    public double clampValue(double value) {
        return Math.max(value, 0);
    }
}
