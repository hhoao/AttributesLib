package dev.shadowsoffire.attributeslib.mixin;

import com.google.common.collect.Ordering;
import dev.shadowsoffire.attributeslib.asm.ALHooks;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a hovering tooltip to the active-potion-effects side panel of inventory screens. 1.12.2
 * vanilla draws only the effect name and duration inline next to each icon, so we layer a
 * mouse-hover tooltip on top via {@link ALHooks#getEffectTooltip(GuiContainer, PotionEffect, List)}
 * which fires {@code GatherEffectScreenTooltipsEvent}.
 *
 * <p>This is the 1.12.2 equivalent of the 1.16+ coremod that patches {@code
 * EffectRenderingInventoryScreen.renderEffects}.
 */
@Mixin(InventoryEffectRenderer.class)
public abstract class InventoryEffectRendererMixin extends GuiContainer {

    @Shadow protected boolean hasActivePotionEffects;

    public InventoryEffectRendererMixin(net.minecraft.inventory.Container c) {
        super(c);
    }

    @Inject(method = "drawScreen(IIF)V", at = @At("RETURN"), require = 1)
    public void apoth_renderEffectTooltips(
            int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!this.hasActivePotionEffects) return;

        Collection<PotionEffect> active =
                Minecraft.getMinecraft().player.getActivePotionEffects();
        if (active.isEmpty()) return;

        int x = this.guiLeft - 124;
        int y = this.guiTop;
        int height = 33;
        if (active.size() > 5) {
            height = 132 / (active.size() - 1);
        }

        for (PotionEffect effect : Ordering.natural().sortedCopy(active)) {
            Potion potion = effect.getPotion();
            if (!potion.shouldRender(effect)) continue;

            if (mouseX >= x && mouseX < x + 140 && mouseY >= y && mouseY < y + 32) {
                List<ITextComponent> tooltip = new ArrayList<>(2);
                if (potion.shouldRenderInvText(effect)) {
                    String name = I18n.format(potion.getName());
                    int amp = effect.getAmplifier();
                    if (amp >= 1 && amp <= 3) {
                        name = name + " " + I18n.format("enchantment.level." + (amp + 1));
                    }
                    tooltip.add(new TextComponentString(name));
                    tooltip.add(new TextComponentString(Potion.getPotionDurationString(effect, 1.0F)));
                } else {
                    tooltip.add(new TextComponentString(I18n.format(potion.getName())));
                    tooltip.add(new TextComponentString(""));
                }

                List<ITextComponent> result = ALHooks.getEffectTooltip(this, effect, tooltip);
                List<String> lines = new ArrayList<>(result.size());
                for (ITextComponent comp : result) {
                    lines.add(comp.getFormattedText());
                }
                this.drawHoveringText(lines, mouseX, mouseY);
                return;
            }

            y += height;
        }
    }
}
