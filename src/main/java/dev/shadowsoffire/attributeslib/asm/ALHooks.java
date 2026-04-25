package dev.shadowsoffire.attributeslib.asm;

import dev.shadowsoffire.attributeslib.api.client.GatherEffectScreenTooltipsEvent;
import java.util.List;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.common.MinecraftForge;

/** Contains coremod-injected hooks. */
public class ALHooks {

    /**
     * Injected immediately after the following line of code: <code><pre>
     * List&lt;Component&gt; list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F));
     * </pre></code> This overrides the value of the list to the event-modified tooltip lines.
     *
     * @param screen The screen rendering the tooltip.
     * @param effectInst The effect instance whose tooltip is being rendered.
     * @param tooltip The existing tooltip lines, which consist of the name and the duration.
     * @return The new tooltip lines, modified by the event.
     */
    public static List<IFormattableTextComponent> getEffectTooltip(
            DisplayEffectsScreen<?> screen,
            EffectInstance effectInst,
            List<IFormattableTextComponent> tooltip) {
        GatherEffectScreenTooltipsEvent event =
                new GatherEffectScreenTooltipsEvent(screen, effectInst, tooltip);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTooltip();
    }
}
