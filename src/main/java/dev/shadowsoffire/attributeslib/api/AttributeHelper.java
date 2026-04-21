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

    public static final UUID BASE_ATTACK_DAMAGE = ItemAccess.getBaseAD();
    public static final UUID BASE_ATTACK_SPEED = ItemAccess.getBaseAS();

    /** UUID of the base modifier for Attack Range. */
    public static final UUID BASE_ENTITY_REACH =
            UUID.fromString("89689aa7-c577-4d97-a03e-791fde1798d4");

    /** UUID of the modifier Elytras use for {@link ALObjects.Attributes#ELYTRA_FLIGHT}. */
    public static final UUID ELYTRA_FLIGHT_UUID =
            UUID.fromString("72aae561-99a9-4a48-9b14-589a255cb077");

    /** UUID of the modifier given to creative players to enable creative flight. */
    public static final UUID CREATIVE_FLIGHT_UUID =
            UUID.fromString("3f54312c-0b60-44ff-bf1e-219091553964");

    /**
     * Applies a permanent modifier to the given attribute. 1.12.2 uses {@link
     * IAttributeInstance#applyModifier(AttributeModifier)}; there is no transient API.
     *
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

    /** Adds the given modifier to the base value of the attribute (operation 0). */
    public static void addToBase(
            EntityLivingBase entity, IAttribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, 0);
    }

    /** Adds (modifier × new base) to the final value (operation 1). */
    public static void addXTimesNewBase(
            EntityLivingBase entity, IAttribute attribute, String name, double modifier) {
        modify(entity, attribute, name, modifier, 1);
    }

    /** Multiplies the final value by (1 + modifier) (operation 2). */
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
