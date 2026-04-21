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
 * Fired when a container screen (inventory or creative) draws a hovered potion-effect tooltip.
 * Listeners may modify the tooltip. 1.12.2 renders effects on {@link GuiContainer} subclasses
 * (e.g. {@code GuiInventory}, {@code GuiContainerCreative}), so the screen argument is typed as
 * such rather than a dedicated effect-rendering class.
 *
 * <p>Fired on {@link MinecraftForge#EVENT_BUS}. Client only ({@link Side#CLIENT}).
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

    public GuiContainer getScreen() {
        return this.screen;
    }

    public PotionEffect getEffectInstance() {
        return this.effectInst;
    }

    public List<ITextComponent> getTooltip() {
        return this.tooltip;
    }
}
