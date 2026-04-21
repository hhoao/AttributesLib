package dev.shadowsoffire.attributeslib.impl;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * A Boolean Attribute displays modifiers as "Enables" or "Forcibly Disables".
 *
 * <ul>
 *   <li>Value 1, {@link AttributeModifier.Operation#ADDITION op=0} — enable.
 *   <li>Value -1, {@link AttributeModifier.Operation#MULTIPLY_TOTAL op=2} — force-disable.
 * </ul>
 */
public class BooleanAttribute extends RangedAttribute implements IFormattableAttribute {

    public BooleanAttribute(@Nullable IAttribute parent, String unlocalizedName, boolean defaultValue) {
        super(parent, unlocalizedName, defaultValue ? 1.0D : 0.0D, 0.0D, 1.0D);
    }

    @Override
    public ITextComponent toValueComponent(
            AttributeModifier.Operation op, double value, ITooltipFlag flag) {
        if (op == null) {
            return new TextComponentTranslation(
                    "attributeslib.value.boolean." + (value > 0 ? "enabled" : "disabled"));
        } else if (op == AttributeModifier.Operation.ADDITION && (int) value == 1) {
            return new TextComponentTranslation("attributeslib.value.boolean.enable");
        } else if (op == AttributeModifier.Operation.MULTIPLY_TOTAL && (int) value == -1) {
            return new TextComponentTranslation("attributeslib.value.boolean.force_disable");
        }
        return new TextComponentTranslation("attributeslib.value.boolean.invalid");
    }

    @Override
    public ITextComponent toComponent(AttributeModifier modif, ITooltipFlag flag) {
        IAttribute attr = this.ths();
        double value = modif.getAmount();

        TextFormatting color = value > 0.0D ? TextFormatting.BLUE : TextFormatting.RED;
        if (value < 0.0D) value *= -1.0D;

        ITextComponent comp =
                new TextComponentTranslation(
                                "attributeslib.modifier.bool",
                                this.toValueComponent(modif.getOperation(), value, flag),
                                new TextComponentTranslation(attr.getName()))
                        .setStyle(new Style().setColor(color));

        ITextComponent debug = this.getDebugInfo(modif, flag);
        if (debug != null && !(debug instanceof TextComponentString
                && ((TextComponentString) debug).getText().isEmpty())) {
            comp.appendSibling(debug);
        }
        return comp;
    }

    @Override
    public double clampValue(double value) {
        return Math.max(value, 0);
    }
}
