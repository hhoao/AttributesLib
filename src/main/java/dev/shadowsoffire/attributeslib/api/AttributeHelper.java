package dev.shadowsoffire.attributeslib.api;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.util.Comparators;
import dev.shadowsoffire.attributeslib.util.ItemAccess;
import java.util.Comparator;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class AttributeHelper {

    /** ID of the base modifier for Attack Damage */
    public static final ResourceLocation BASE_ATTACK_DAMAGE = ItemAccess.getBaseAD();

    /** ID of the base modifier for Attack Speed */
    public static final ResourceLocation BASE_ATTACK_SPEED = ItemAccess.getBaseAS();

    /** ID of the base modifier for entity interaction range. */
    public static final ResourceLocation BASE_ENTITY_REACH =
            AttributesLib.loc("fake_base_range");

    /** ID of the modifier Elytras use for {@link ALObjects.Attributes#ELYTRA_FLIGHT}. */
    public static final ResourceLocation ELYTRA_FLIGHT_ID =
            AttributesLib.loc("elytra_item_flight");

    /**
     * ID of the modifier given to creative players to enable {@link
     * ALObjects.Attributes#CREATIVE_FLIGHT}.
     */
    public static final ResourceLocation CREATIVE_FLIGHT_ID =
            AttributesLib.loc("creative_flight");

    /**
     * A brief explanation of {@link Operation} and Attribute calculations:
     *
     * <p>Each Attribute Modifier for a specific attribute provides an Operation and a value.<br>
     * The meaning of the value depends on the operation.
     *
     * <p>There are three valid operations: Addition, Multiply Base, and Multiply Total. They are
     * executed in order.<br>
     *
     * <ol>
     *   <li>{@link Operation#ADD_VALUE Addition} adds the given modifier to the base value of the
     *       attribute.
     *   <li>{@link Operation#ADD_MULTIPLIED_BASE Multiply Base} adds (modifier * new base value) to the
     *       final value.
     *   <li>{@link Operation#ADD_MULTIPLIED_TOTAL Multiply Total} multiplies the final value by (1.0 +
     *       modifier).
     * </ol>
     *
     * The Attribute has the ability to clamp the final modified value, so the result of some
     * modifiers may be ignored.
     *
     * <p>For example, given an attribute with a base value of 1, applying an Addition modifier with
     * a value of 1 would result in a value of 2 (1 + 1).<br>
     * Additionally applying a Multiply Base modifier with a value of 1.5 would result in a value of
     * (2 + 1.5 * 2).<br>
     * Further applying a Multiply Total modifier with a value of 0.75 would result in a value of
     * 8.75 (5.0 * (1 + 0.75)).<br>
     *
     * <p>Applies a permanent modifier to the given attribute via {@link
     * AttributeInstance#addPermanentModifier(AttributeModifier)}.
     *
     * @param entity The entity the modifier will be applied to.
     * @param attribute The attribute being modified.
     * @param name The name of the attribute modifier.
     * @param value The value of the attribute modifier. See above.
     * @param operation The operation of the attribute modifier. See above.
     * @see AttributeInstance#calculateValue()
     */
    public static void modify(
            LivingEntity entity,
            Attribute attribute,
            String name,
            double value,
            Operation operation) {
        AttributeInstance inst = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
        if (inst != null)
            inst.addPermanentModifier(
                    new AttributeModifier(AttributesLib.loc(name), value, operation));
    }

    public static void modify(
            LivingEntity entity,
            Holder<Attribute> attribute,
            String name,
            double value,
            Operation operation) {
        AttributeInstance inst = entity.getAttribute(attribute);
        if (inst != null)
            inst.addPermanentModifier(new AttributeModifier(AttributesLib.loc(name), value, operation));
    }

    /** Adds the given modifier to the base value of the attribute. */
    public static void addToBase(
            LivingEntity entity, Attribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, Operation.ADD_VALUE);
    }

    /**
     * Adds (modifier * new base value) to the final value of the attribute. New base value is the
     * base value plus all additions (operation 0 AttributeModifiers).
     */
    public static void addXTimesNewBase(
            LivingEntity entity, Attribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, Operation.ADD_MULTIPLIED_BASE);
    }

    /**
     * Multiplies the final value of this attribute by 1.0 + modifier. Final value is the value
     * after computing all operation 0 and 1 AttributeModifiers.
     */
    public static void multiplyFinal(
            LivingEntity entity, Attribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, Operation.ADD_MULTIPLIED_TOTAL);
    }

    @SuppressWarnings("deprecation")
    public static Multimap<Attribute, AttributeModifier> sortedMap() {
        return TreeMultimap.create(
                Comparators.idComparator(BuiltInRegistries.ATTRIBUTE), modifierComparator());
    }

    public static Comparator<AttributeModifier> modifierComparator() {
        return Comparators.chained(
                Comparator.comparing(AttributeModifier::operation),
                Comparator.comparing(AttributeModifier::amount),
                Comparator.comparing(AttributeModifier::id));
    }

    /** Creates a mutable component starting with the char used to represent a drop-down list. */
    public static MutableComponent list() {
        return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
    }
}
