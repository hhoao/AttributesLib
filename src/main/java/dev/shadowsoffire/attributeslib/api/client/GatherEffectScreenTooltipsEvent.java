package dev.shadowsoffire.attributeslib.api.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This event is called when a container screen draws the tooltip lines for a hovered
 * {@link PotionEffect}.<br>
 * It can be used to modify the tooltip.
 *
 * <p>1.12.2 renders effects on {@link GuiContainer} subclasses (e.g. {@code GuiInventory},
 * {@code GuiContainerCreative}), so the screen argument is typed as such rather than a dedicated
 * effect-rendering class.
 *
 * <p>This event is fired on {@linkplain MinecraftForge#EVENT_BUS the main event bus}.<br>
 * This event is only fired on the {@linkplain Side#CLIENT physical client}.
 */
public class GatherEffectScreenTooltipsEvent extends Event {

    protected final GuiContainer screen;
    protected final PotionEffect effectInst;
    protected final List<ITextComponent> tooltip;

    public GatherEffectScreenTooltipsEvent(
            GuiContainer screen, PotionEffect effectInst, List<ITextComponent> tooltip) {
        this.screen = screen;
        this.effectInst = effectInst;
        this.tooltip = new ArrayList<>(tooltip);
    }

    /** @return The screen which will be rendering the tooltip lines. */
    public GuiContainer getScreen() {
        return this.screen;
    }

    /** @return The effect whose tooltip is being drawn. */
    public PotionEffect getEffectInstance() {
        return this.effectInst;
    }

    /** @return A mutable list of tooltip lines. */
    public List<ITextComponent> getTooltip() {
        return this.tooltip;
    }
}
