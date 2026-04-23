package dev.shadowsoffire.attributeslib.api;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.util.Comparators;
import dev.shadowsoffire.attributeslib.util.ItemAccess;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AttributeHelper {

    /** UUID of the base modifier for Attack Damage */
    public static final UUID BASE_ATTACK_DAMAGE = ItemAccess.getBaseAD();

    /** UUID of the base modifier for Attack Speed */
    public static final UUID BASE_ATTACK_SPEED = ItemAccess.getBaseAS();

    /** UUID of the base modifier for Attack Range */
    public static final UUID BASE_ENTITY_REACH =
            UUID.fromString("89689aa7-c577-4d97-a03e-791fde1798d4");

    /** UUID of the modifier Elytras use for {@link ALObjects.Attributes#ELYTRA_FLIGHT}. */
    public static final UUID ELYTRA_FLIGHT_UUID =
            UUID.fromString("72aae561-99a9-4a48-9b14-589a255cb077");

    /**
     * UUID of the modifier given to creative players to enable {@link
     * ALObjects.Attributes#CREATIVE_FLIGHT}.
     */
    public static final UUID CREATIVE_FLIGHT_UUID =
            UUID.fromString("3f54312c-0b60-44ff-bf1e-219091553964");

    /**
     * A brief explanation of operations and Attribute calculations:
     *
     * <p>Each Attribute Modifier for a specific attribute provides an Operation and a value.<br>
     * The meaning of the value depends on the operation.
     *
     * <p>There are three valid operations: Addition, Multiply Base, and Multiply Total. They are
     * executed in order.<br>
     *
     * <ol>
     *   <li>Addition ({@code 0}) adds the given modifier to the base value of the attribute.
     *   <li>Multiply Base ({@code 1}) adds (modifier * new base value) to the final value.
     *   <li>Multiply Total ({@code 2}) multiplies the final value by (1.0 + modifier).
     * </ol>
     *
     * The Attribute has the ability to clamp the final modified value, so the result of some
     * modifiers may be ignored.
     *
     * <p>For example, given an attribute with a base value of 1, applying an Addition modifier
     * with a value of 1 would result in a value of 2 (1 + 1).<br>
     * Additionally applying a Multiply Base modifier with a value of 1.5 would result in a value
     * of 5.0 (2 + 1.5 * 2).<br>
     * Further applying a Multiply Total modifier with a value of 0.75 would result in a value of
     * 8.75 (5.0 * (1 + 0.75)).<br>
     *
     * <p>Applies a permanent modifier to the given attribute. 1.12.2 uses {@link
     * IAttributeInstance#applyModifier(AttributeModifier)}; there is no transient API.
     *
     * @param entity The entity the modifier will be applied to.
     * @param attribute The attribute being modified.
     * @param name The name of the attribute modifier.
     * @param value The value of the attribute modifier. See above.
     * @param operation 0=ADDITION, 1=MULTIPLY_BASE, 2=MULTIPLY_TOTAL (the {@link AttributeModifier}
     *     constructor takes the int form directly).
     */
    public static void modify(
            EntityLivingBase entity,
            IAttribute attribute,
            String name,
            double value,
            int operation) {
        IAttributeInstance inst = entity.getEntityAttribute(attribute);
        if (inst != null) {
            inst.applyModifier(
                    new AttributeModifier(AttributesLib.MODID + ":" + name, value, operation));
        }
    }

    /** Adds the given modifier to the base value of the attribute. */
    public static void addToBase(
            EntityLivingBase entity, IAttribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, 0);
    }

    /**
     * Adds (modifier * new base value) to the final value of the attribute. New base value is the
     * base value plus all additions (operation 0 AttributeModifiers).
     */
    public static void addXTimesNewBase(
            EntityLivingBase entity, IAttribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, 1);
    }

    /**
     * Multiplies the final value of this attribute by 1.0 + modifier. Final value is the value
     * after computing all operation 0 and 1 AttributeModifiers.
     */
    public static void multiplyFinal(
            EntityLivingBase entity, IAttribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, 2);
    }

    public static Multimap<IAttribute, AttributeModifier> sortedMap() {
        return TreeMultimap.create(Comparators.attributeNameComparator(), modifierComparator());
    }

    public static Comparator<AttributeModifier> modifierComparator() {
        return Comparators.chained(
                Comparator.comparing(AttributeModifier::getOperation),
                Comparator.comparing(AttributeModifier::getAmount),
                Comparator.comparing(AttributeModifier::getOperation));
    }

    /** Creates a mutable component starting with the char used to represent a drop-down list. */
    public static ITextComponent list() {
        return new TextComponentString(" ┇ ").setStyle(new Style().setColor(TextFormatting.GRAY));
    }
}
