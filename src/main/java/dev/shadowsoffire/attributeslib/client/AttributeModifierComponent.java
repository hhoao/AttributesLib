package dev.shadowsoffire.attributeslib.client;

import dev.shadowsoffire.attributeslib.AttributesLib;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class AttributeModifierComponent implements ClientTooltipComponent {

    public static final ResourceLocation TEXTURE =
            AttributesLib.loc("textures/gui/attribute_component.png");

    @Nullable private final ModifierSource<?> source;
    private final List<FormattedCharSequence> text;

    public AttributeModifierComponent(
            @Nullable ModifierSource<?> source, FormattedText text, Font font, int maxWidth) {
        this.source = source;
        this.text = font.split(text, maxWidth);
    }

    @Override
    public int getHeight(Font font) {
        return this.text.size() * 10;
    }

    @Override
    public int getWidth(Font font) {
        return this.text.stream().map(font::width).map(w -> w + 12).max(Integer::compareTo).get();
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics gfx) {
        gfx.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x,
                y,
                0,
                this.source == null ? 9 : 0,
                9,
                9,
                18,
                9);
        if (this.source == null) return;
        this.source.render(gfx, font, x, y);
    }

    @Override
    public void renderText(GuiGraphics gfx, Font font, int pX, int pY) {
        gfx.drawString(font, this.text.get(0), pX + 12, pY, -1, true);
        for (int i = 1; i < this.text.size(); i++) {
            gfx.drawString(font, this.text.get(i), pX, pY + i * (font.lineHeight + 1), -1, true);
        }
    }
}
