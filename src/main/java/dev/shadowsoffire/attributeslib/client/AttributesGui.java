package dev.shadowsoffire.attributeslib.client;

/**
 * 1.16.4's inventory-side attribute panel relied on {@code Widget}/{@code ImageButton}/{@code
 * MatrixStack}, none of which exist in 1.12.2. This stub preserves the type for future work on a
 * {@code GuiButton}-based replacement.
 */
public class AttributesGui {

    public static boolean wasOpen = false;
    public static boolean swappedFromCurios = false;

    public AttributesGui() {}

    public void toggleVisibility() {}
}
