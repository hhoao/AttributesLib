package dev.shadowsoffire.attributeslib.client;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

/**
 * 1.12.2 replacement for the modern custom tooltip component. This helper only prepares wrapped
 * text lines with an optional source prefix so the attributes GUI can render modifier details in a
 * readable format.
 */
public class AttributeModifierComponent {

    private final List<String> lines;

    public AttributeModifierComponent(
            @Nullable ModifierSource<?> source, ITextComponent text, FontRenderer font, int maxWidth) {
        String rendered = source == null ? text.getFormattedText() : source.getLabel() + " " + text.getFormattedText();
        this.lines = Collections.unmodifiableList(font.listFormattedStringToWidth(rendered, maxWidth));
    }

    public List<String> getLines() {
        return this.lines;
    }
}
