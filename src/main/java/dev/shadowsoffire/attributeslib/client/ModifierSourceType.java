package dev.shadowsoffire.attributeslib.client;

import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.attributeslib.client.ModifierSource.EffectModifierSource;
import dev.shadowsoffire.attributeslib.client.ModifierSource.ItemModifierSource;
import dev.shadowsoffire.attributeslib.util.Comparators;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

public abstract class ModifierSourceType {

    private static final List<ModifierSourceType> SOURCE_TYPES = new ArrayList<>();

    public static final ModifierSourceType EQUIPMENT =
            register(
                    new ModifierSourceType() {
                        @Override
                        public void extract(
                                EntityLivingBase entity,
                                BiConsumer<AttributeModifier, ModifierSource<?>> consumer) {
                            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                                ItemStack stack = entity.getItemStackFromSlot(slot);
                                stack.getAttributeModifiers(slot)
                                        .values()
                                        .forEach(
                                                modifier ->
                                                        consumer.accept(
                                                                modifier,
                                                                new ItemModifierSource(
                                                                        stack, slot)));
                            }
                        }

                        @Override
                        public int getPriority() {
                            return 0;
                        }
                    });

    public static final ModifierSourceType MOB_EFFECT =
            register(
                    new ModifierSourceType() {
                        @Override
                        public void extract(
                                EntityLivingBase entity,
                                BiConsumer<AttributeModifier, ModifierSource<?>> consumer) {
                            for (PotionEffect effect : entity.getActivePotionEffects()) {
                                effect.getPotion()
                                        .getAttributeModifierMap()
                                        .values()
                                        .forEach(
                                                modifier ->
                                                        consumer.accept(
                                                                modifier,
                                                                new EffectModifierSource(effect)));
                            }
                        }

                        @Override
                        public int getPriority() {
                            return 100;
                        }
                    });

    public static Collection<ModifierSourceType> getTypes() {
        return Collections.unmodifiableCollection(SOURCE_TYPES);
    }

    public static <T extends ModifierSourceType> T register(T type) {
        SOURCE_TYPES.add(type);
        return type;
    }

    public static Comparator<AttributeModifier> compareBySource(
            Map<UUID, ModifierSource<?>> sources) {

        Comparator<AttributeModifier> comp =
                Comparators.chained(
                        Comparator.comparingInt(
                                a -> sources.get(a.getID()).getType().getPriority()),
                        Comparator.comparing(a -> sources.get(a.getID())),
                        AttributeHelper.modifierComparator());

        return (a1, a2) -> {
            ModifierSource<?> src1 = sources.get(a1.getID());
            ModifierSource<?> src2 = sources.get(a2.getID());

            if (src1 != null && src2 != null) return comp.compare(a1, a2);

            return src1 != null ? -1 : src2 != null ? 1 : 0;
        };
    }

    public abstract void extract(
            EntityLivingBase entity, BiConsumer<AttributeModifier, ModifierSource<?>> consumer);

    public abstract int getPriority();
}
