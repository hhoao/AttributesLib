package dev.shadowsoffire.attributeslib.util;

import java.util.Comparator;
import net.minecraft.entity.ai.attributes.IAttribute;

/** Misc Comparator Utils. */
public class Comparators {

    @SafeVarargs
    public static <T> Comparator<T> chained(Comparator<T>... comparators) {
        Comparator<T> c = comparators[0];
        for (int i = 1; i < comparators.length; i++) {
            c = c.thenComparing(comparators[i]);
        }
        return c;
    }

    /**
     * 1.12.2 has no attribute registry; order attributes by their unlocalised name which serves as a
     * stable identifier.
     */
    public static Comparator<IAttribute> attributeNameComparator() {
        return Comparator.comparing(IAttribute::getName);
    }
}
