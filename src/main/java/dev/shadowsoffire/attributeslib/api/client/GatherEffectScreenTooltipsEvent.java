package dev.shadowsoffire.attributeslib.api.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is called when a {@link EffectRenderingInventoryScreen} draws the tooltip lines for a
 * hovered {@link MobEffectInstance}.<br>
 * It can be used to modify the tooltip.
 *
 * <p>This event is fired on {@linkplain MinecraftForge#EVENT_BUS the main event bus}.<br>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class GatherEffectScreenTooltipsEvent extends Event {

    protected final DisplayEffectsScreen<?> screen;
    protected final EffectInstance effectInst;
    protected final List<IFormattableTextComponent> tooltip;

    public GatherEffectScreenTooltipsEvent(
            DisplayEffectsScreen<?> screen,
            EffectInstance effectInst,
            List<IFormattableTextComponent> tooltip) {
        this.screen = screen;
        this.effectInst = effectInst;
        this.tooltip = new ArrayList<>(tooltip);
    }

    /** @return The screen which will be rendering the tooltip lines. */
    public DisplayEffectsScreen<?> getScreen() {
        return this.screen;
    }

    /** @return The effect whose tooltip is being drawn. */
    public EffectInstance getEffectInstance() {
        return this.effectInst;
    }

    /** @return A mutable list of tooltip lines. */
    public List<IFormattableTextComponent> getTooltip() {
        return this.tooltip;
    }
}
