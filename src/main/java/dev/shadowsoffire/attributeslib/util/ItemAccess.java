package dev.shadowsoffire.attributeslib.util;

import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.item.Item;

/**
 * Resolve vanilla's base attack modifier UUIDs without linking against a Forge-internal helper
 * whose binary signature differs across 1.12.2 builds.
 */
public final class ItemAccess {

    private static final UUID BASE_ATTACK_DAMAGE = getStaticUuid("ATTACK_DAMAGE_MODIFIER", "field_111210_e");
    private static final UUID BASE_ATTACK_SPEED = getStaticUuid("ATTACK_SPEED_MODIFIER", "field_185050_h");

    private ItemAccess() {}

    private static UUID getStaticUuid(String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = Item.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (UUID) field.get(null);
            } catch (ReflectiveOperationException ex) {
                // Try the next name variant.
            }
        }
        throw new IllegalStateException("Unable to resolve Item UUID field: " + String.join(", ", fieldNames));
    }

    public static UUID getBaseAD() {
        return BASE_ATTACK_DAMAGE;
    }

    public static UUID getBaseAS() {
        return BASE_ATTACK_SPEED;
    }
}
