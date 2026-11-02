package dev.shadowsoffire.attributeslib.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link GuiContainer}'s {@code guiLeft} and {@code guiTop} layout fields, which are
 * protected and therefore unreachable from the attributes panel.
 *
 * <p>An accessor is used rather than reflection because the field names only match the MCP
 * mappings in a dev workspace; at runtime they are the obfuscated names, so
 * {@code getDeclaredField("guiLeft")} throws. The refmap resolves the mapping in both cases.
 */
@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {

    @Accessor("guiLeft")
    int getGuiLeft();

    @Accessor("guiTop")
    int getGuiTop();
}
