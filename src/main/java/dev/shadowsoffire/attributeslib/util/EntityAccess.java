package dev.shadowsoffire.attributeslib.util;

import java.lang.reflect.Field;
import net.minecraft.entity.Entity;

/** Accessors for fields that stay non-public in parts of the 1.12.2 toolchain. */
public final class EntityAccess {

    private EntityAccess() {}

    public static int getFireTicks(Entity entity) {
        return (Integer) getFieldValue(entity, "fire", "field_190534_ay");
    }

    private static Object getFieldValue(Object target, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = Entity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ex) {
                // Try the next name variant.
            }
        }
        throw new IllegalStateException("Unable to resolve Entity field: " + String.join(", ", fieldNames));
    }
}
