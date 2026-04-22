package dev.shadowsoffire.attributeslib.asm;

import dev.shadowsoffire.attributeslib.api.client.GatherEffectScreenTooltipsEvent;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;

/** Shared entry points invoked from mixins. */
public class ALHooks {

    /**
     * Fires {@link GatherEffectScreenTooltipsEvent} so listeners can modify the tooltip rendered
     * next to a potion-effect icon in inventory/creative GUIs, and returns the possibly-mutated
     * list.
     *
     * @param screen     The container screen rendering the tooltip.
     * @param effectInst The potion effect whose tooltip is being rendered.
     * @param tooltip    The existing tooltip lines (name, duration).
     * @return           The tooltip lines after event dispatch.
     */
    public static List<ITextComponent> getEffectTooltip(
            GuiContainer screen, PotionEffect effectInst, List<ITextComponent> tooltip) {
        GatherEffectScreenTooltipsEvent event =
                new GatherEffectScreenTooltipsEvent(screen, effectInst, tooltip);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTooltip();
    }
}
