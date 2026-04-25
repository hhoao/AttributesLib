package dev.shadowsoffire.attributeslib.impl;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A Percentile Based Attribute is one which always displays modifiers as percentages, even addition
 * ones.<br>
 * This is used for attributes that would not make sense being displayed as flat additions (ex +0.05
 * Life Steal).
 */
public class PercentBasedAttribute extends RangedAttribute implements IFormattableAttribute {

    public PercentBasedAttribute(
            String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
    }

    @Override
    public IFormattableTextComponent toComponent(AttributeModifier modif, ITooltipFlag flag) {
        return IFormattableAttribute.super.toComponent(modif, flag);
    }

    @Override
    public IFormattableTextComponent toValueComponent(
            AttributeModifier.Operation op, double value, ITooltipFlag flag) {
        return new TranslationTextComponent(
                "attributeslib.value.percent", ItemStack.DECIMALFORMAT.format(value * 100));
    }
}
