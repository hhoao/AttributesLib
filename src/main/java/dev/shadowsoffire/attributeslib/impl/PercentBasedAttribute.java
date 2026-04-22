package dev.shadowsoffire.attributeslib.impl;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * A Percentile Based Attribute always displays modifiers as percentages, even addition ones. Used
 * for attributes where flat additions would be unreadable (e.g. +0.05 Life Steal → shown as 5%).
 */
public class PercentBasedAttribute extends RangedAttribute implements IFormattableAttribute {

    public PercentBasedAttribute(
            @Nullable IAttribute parent,
            String unlocalizedName,
            double defaultValue,
            double min,
            double max) {
        super(parent, unlocalizedName, defaultValue, min, max);
    }

    @Override
    public ITextComponent toComponent(AttributeModifier modif, ITooltipFlag flag) {
        return IFormattableAttribute.super.toComponent(modif, flag);
    }

    @Override
    public ITextComponent toValueComponent(
            @Nullable Integer op, double value, ITooltipFlag flag) {
        return new TextComponentTranslation(
                "attributeslib.value.percent", ItemStack.DECIMALFORMAT.format(value * 100));
    }
}
