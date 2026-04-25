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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;

/**
 * A Modifier Source Type is a the registration component of a ModifierSource.
 *
 * @param <T>
 */
public abstract class ModifierSourceType {

    private static final List<ModifierSourceType> SOURCE_TYPES = new ArrayList<>();

    public static final ModifierSourceType EQUIPMENT =
            register(
                    new ModifierSourceType() {
                        @Override
                        public void extract(
                                LivingEntity entity,
                                BiConsumer<AttributeModifier, ModifierSource<?>> map) {
                            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                                ItemStack item = entity.getItemStackFromSlot(slot);
                                item.getAttributeModifiers(slot)
                                        .values()
                                        .forEach(
                                                modif -> {
                                                    map.accept(modif, new ItemModifierSource(item));
                                                });
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
                                LivingEntity entity,
                                BiConsumer<AttributeModifier, ModifierSource<?>> map) {
                            for (EffectInstance effectInst : entity.getActivePotionEffects()) {
                                effectInst
                                        .getPotion()
                                        .getAttributeModifierMap()
                                        .values()
                                        .forEach(
                                                modif -> {
                                                    map.accept(
                                                            modif,
                                                            new EffectModifierSource(effectInst));
                                                });
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

    /**
     * Extracts all ModifierSource(s) of this type from the source entity.
     *
     * @param entity
     * @param map
     */
    public abstract void extract(
            LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map);

    /**
     * Integer priority for display sorting.<br>
     * Lower priority values will be displayed at the top of the list.
     */
    public abstract int getPriority();
}
