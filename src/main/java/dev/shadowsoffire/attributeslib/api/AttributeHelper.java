package dev.shadowsoffire.attributeslib.api;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.util.Comparators;
import dev.shadowsoffire.attributeslib.util.ItemAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

import java.util.Comparator;
import java.util.UUID;

public class AttributeHelper {

    /**
     * UUID of the base modifier for Attack Damage
     */
    public static final UUID BASE_ATTACK_DAMAGE = ItemAccess.getBaseAD();

    /**
     * UUID of the base modifier for Attack Speed
     */
    public static final UUID BASE_ATTACK_SPEED = ItemAccess.getBaseAS();

    /**
     * UUID of the base modifier for Attack Range
     */
    public static final UUID BASE_ENTITY_REACH = UUID.fromString("89689aa7-c577-4d97-a03e-791fde1798d4");

    /**
     * UUID of the modifier Elytras use for {@link ALObjects.Attributes#ELYTRA_FLIGHT}.
     */
    public static final UUID ELYTRA_FLIGHT_UUID = UUID.fromString("72aae561-99a9-4a48-9b14-589a255cb077");

    /**
     * UUID of the modifier given to creative players to enable {@link ALObjects.Attributes#CREATIVE_FLIGHT}.
     */
    public static final UUID CREATIVE_FLIGHT_UUID = UUID.fromString("3f54312c-0b60-44ff-bf1e-219091553964");

    /**
     * A brief explanation of {@link Operation} and Attribute calculations:
     * <p>
     * Each Attribute Modifier for a specific attribute provides an Operation and a value.<br>
     * The meaning of the value depends on the operation.
     * <p>
     * There are three valid operations: Addition, Multiply Base, and Multiply Total. They are executed in order.<br>
     * <ol>
     * <li>{@link Operation#ADDITION Addition} adds the given modifier to the base value of the attribute.</li>
     * <li>{@link Operation#MULTIPLY_BASE Multiply Base} adds (modifier * new base value) to the final value.</li>
     * <li>{@link Operation#MULTIPLY_TOTAL Multiply Total} multiplies the final value by (1.0 + modifier).</li>
     * </ol>
     * The Attribute has the ability to clamp the final modified value, so the result of some modifiers may be ignored.
     * <p>
     * For example, given an attribute with a base value of 1, applying an Addition modifier with a value of 1 would result in a value of 2 (1 + 1).<br>
     * Additionally applying a Multiply Base modifier with a value of 1.5 would result in a value of (2 + 1.5 * 2).<br>
     * Further applying a Multiply Total modifier with a value of 0.75 would result in a value of 8.75 (5.0 * (1 + 0.75)).<br>
     * <p>
     * Applies a permanent modifier to the given attribute via {@link AttributeInstance#addPermanentModifier(AttributeModifier)}.
     *
     * @param entity    The entity the modifier will be applied to.
     * @param attribute The attribute being modified.
     * @param name      The name of the attribute modifier.
     * @param value     The value of the attribute modifier. See above.
     * @param operation The operation of the attribute modifier. See above.
     * @see AttributeInstance#calculateValue()
     */
    public static void modify(LivingEntity entity, Attribute attribute, String name, double value, Operation operation) {
        AttributeInstance inst = entity.getAttribute(attribute);
        if (inst != null) inst.addPermanentModifier(new AttributeModifier(AttributesLib.MODID + ":" + name, value, operation));
    }

    /**
     * Adds the given modifier to the base value of the attribute.
     */
    public static void addToBase(LivingEntity entity, Attribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, Operation.ADDITION);
    }

    /**
     * Adds (modifier * new base value) to the final value of the attribute.
     * New base value is the base value plus all additions (operation 0 AttributeModifiers).
     */
    public static void addXTimesNewBase(LivingEntity entity, Attribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, Operation.MULTIPLY_BASE);
    }

    /**
     * Multiplies the final value of this attribute by 1.0 + modifier.
     * Final value is the value after computing all operation 0 and 1 AttributeModifiers.
     */
    public static void multiplyFinal(LivingEntity entity, Attribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, Operation.MULTIPLY_TOTAL);
    }

    @SuppressWarnings("deprecation")
    public static Multimap<Attribute, AttributeModifier> sortedMap() {
        return TreeMultimap.create(Comparators.idComparator(BuiltInRegistries.ATTRIBUTE), modifierComparator());
    }

    public static Comparator<AttributeModifier> modifierComparator() {
        return Comparators.chained(
            Comparator.comparing(AttributeModifier::getOperation),
            Comparator.comparing(AttributeModifier::getAmount),
            Comparator.comparing(AttributeModifier::getId));
    }

    /**
     * Creates a mutable component starting with the char used to represent a drop-down list.
     */
    public static MutableComponent list() {
        return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
    }
}
