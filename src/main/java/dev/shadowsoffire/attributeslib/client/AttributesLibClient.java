package dev.shadowsoffire.attributeslib.client;

import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.attributeslib.api.client.GatherEffectScreenTooltipsEvent;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleCrit;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AttributesLibClient {

    /**
     * Bootstraps the client-side event handlers. Called from {@code AttributesLib.preInit} when the
     * side is client. Replaces 1.16.4's constructor-time {@code getModEventBus().register(...)} and
     * {@code AddReloadListenerEvent} subscription.
     */
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new AttributesLibClient());
        IReloadableResourceManager resMgr =
                (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        resMgr.registerReloadListener(ALConfig.makeReloader());
    }

    /**
     * Client entry for {@code CritParticleMessage}: spawn a tinted crit particle on the target
     * entity. 1.12.2 has no particle-emitter API, so we add a single one-shot particle directly.
     */
    public static void apothCrit(int entityId) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;
        Entity entity = mc.world.getEntityByID(entityId);
        if (entity == null) return;
        double x = entity.posX;
        double y = entity.posY + entity.height * 0.5D;
        double z = entity.posZ;
        mc.effectRenderer.addEffect(new ApothCritParticle(mc.world, x, y, z, 0, 0, 0));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void effectGuiTooltips(GatherEffectScreenTooltipsEvent e) {
        List<ITextComponent> tooltips = e.getTooltip();
        PotionEffect effectInst = e.getEffectInstance();
        Potion effect = effectInst.getPotion();

        ITextComponent name = tooltips.get(0);
        ITextComponent duration = tooltips.remove(1);
        duration =
                new TextComponentTranslation("(%s)", duration)
                        .setStyle(new Style().setColor(TextFormatting.WHITE));
        name.appendSibling(new TextComponentString(" ")).appendSibling(duration);

        if (AttributesLib.getTooltipFlag().isAdvanced()) {
            ResourceLocation key = Potion.REGISTRY.getNameForObject(effect);
            name.appendSibling(new TextComponentString(" "))
                    .appendSibling(
                            new TextComponentTranslation("[%s]", key == null ? "" : key.toString())
                                    .setStyle(new Style().setColor(TextFormatting.GRAY)));
        }

        String descKey = effect.getName() + ".desc";
        if (I18n.hasKey(descKey)) {
            tooltips.add(
                    new TextComponentTranslation(descKey)
                            .setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
        }

        Map<IAttribute, AttributeModifier> map = effect.getAttributeModifierMap();
        for (Map.Entry<IAttribute, AttributeModifier> entry : map.entrySet()) {
            AttributeModifier base = entry.getValue();
            AttributeModifier scaled =
                    new AttributeModifier(
                            base.getName(),
                            effect.getAttributeModifierAmount(effectInst.getAmplifier(), base),
                            base.getOperation());
            tooltips.add(
                    IFormattableAttribute.toComponent(
                            entry.getKey(), scaled, AttributesLib.getTooltipFlag()));
        }
    }

    @SubscribeEvent
    public void potionTooltips(ItemTooltipEvent e) {
        if (!ALConfig.enablePotionTooltips) return;
        ItemStack stack = e.getItemStack();
        if (!(stack.getItem() instanceof ItemPotion)) return;

        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
        if (effects.size() != 1) return;

        List<String> tooltips = e.getToolTip();
        if (tooltips.size() < 2) return;

        Potion effect = effects.get(0).getPotion();
        String descKey = effect.getName() + ".desc";
        if (I18n.hasKey(descKey)) {
            tooltips.add(2, TextFormatting.DARK_GRAY + I18n.format(descKey));
        } else if (e.getFlags().isAdvanced() && effect.getAttributeModifierMap().isEmpty()) {
            tooltips.add(
                    2,
                    TextFormatting.DARK_GRAY.toString()
                            + TextFormatting.ITALIC
                            + I18n.format(descKey));
        }
    }

    public static class ApothCritParticle extends ParticleCrit {
        public ApothCritParticle(
                World world, double x, double y, double z, double dx, double dy, double dz) {
            super(world, x, y, z, dx, dy, dz);
            this.setRBGColorF(0.3F, 0.8F, 1F);
        }
    }
}
