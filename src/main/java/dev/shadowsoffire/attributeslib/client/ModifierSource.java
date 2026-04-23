package dev.shadowsoffire.attributeslib.client;

import java.util.Comparator;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;

/**
 * A small wrapper that tracks where an attribute modifier came from so the 1.12.2 attributes GUI
 * can present useful provenance information without the modern tooltip-component pipeline.
 */
public abstract class ModifierSource<T> implements Comparable<ModifierSource<T>> {

    protected final ModifierSourceType type;
    protected final Comparator<T> comparator;
    protected final T data;

    protected ModifierSource(ModifierSourceType type, Comparator<T> comparator, T data) {
        this.type = type;
        this.comparator = comparator;
        this.data = data;
    }

    public ModifierSourceType getType() {
        return this.type;
    }

    public T getData() {
        return this.data;
    }

    public abstract String getLabel();

    @Override
    public int compareTo(ModifierSource<T> other) {
        return this.comparator.compare(this.data, other.data);
    }

    public static class ItemModifierSource extends ModifierSource<ItemStack> {

        private final EntityEquipmentSlot slot;

        public ItemModifierSource(ItemStack stack, EntityEquipmentSlot slot) {
            super(
                    ModifierSourceType.EQUIPMENT,
                    Comparator.comparing(item -> item.getDisplayName(), String.CASE_INSENSITIVE_ORDER),
                    stack);
            this.slot = slot;
        }

        @Override
        public String getLabel() {
            return TextFormatting.GRAY
                    + "["
                    + this.slot.getName()
                    + ": "
                    + this.data.getDisplayName()
                    + "]";
        }
    }

    public static class EffectModifierSource extends ModifierSource<PotionEffect> {

        public EffectModifierSource(PotionEffect effect) {
            super(
                    ModifierSourceType.MOB_EFFECT,
                    Comparator.comparing(
                            inst -> inst.getPotion().getName(), String.CASE_INSENSITIVE_ORDER),
                    effect);
        }

        @Override
        public String getLabel() {
            return TextFormatting.GRAY + "[" + this.data.getEffectName() + "]";
        }
    }
}
