package dev.shadowsoffire.attributeslib.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.attributeslib.api.client.AddAttributeTooltipsEvent;
import dev.shadowsoffire.attributeslib.api.client.GatherEffectScreenTooltipsEvent;
import dev.shadowsoffire.attributeslib.api.client.GatherSkippedAttributeTooltipsEvent;
import dev.shadowsoffire.attributeslib.util.IFlying;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttributesLibClient {

    @SubscribeEvent
    public void updateClientFlyStateOnRespawn(ClientPlayerNetworkEvent.RespawnEvent e) {
        // Teleporting to another dimension constitutes a respawn - the other checks we have ensure
        // the mayFly state returns, but not the flying state.
        // For this one we have to ensure that the state is marked to be restored before
        // MultiPlayerGameMode#adjustPlayer is called in
        // ClientPacketListener#handleRespawn.
        if (e.getOldPlayer().abilities.isFlying) {
            ((IFlying) e.getNewPlayer()).markFlying();
        }
    }

    @SubscribeEvent
    public void clientReload(AddReloadListenerEvent e) {
        e.addListener(ALConfig.makeReloader());
    }

    @SubscribeEvent
    public static void particleFactories(ParticleFactoryRegisterEvent e) {
        Minecraft.getInstance()
                .particles
                .registerFactory(
                        ALObjects.Particles.APOTH_CRIT.get(),
                        spriteSet ->
                                (particleOptions, clientLevel, x, y, z, dx, dy, dz) -> {
                                    ApothCritParticle apothCritParticle =
                                            new ApothCritParticle(
                                                    ALObjects.Particles.APOTH_CRIT.get(),
                                                    clientLevel,
                                                    x,
                                                    y,
                                                    z,
                                                    dx,
                                                    dy,
                                                    dz);
                                    apothCritParticle.selectSpriteRandomly(spriteSet);
                                    //
                                    // apothCritParticle.pickSprite(spriteSet);
                                    return apothCritParticle;
                                });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tooltips(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        List<ITextComponent> list = e.getToolTip();
        int markIdx1 = -1, markIdx2 = -1;
        for (int i = 0; i < list.size(); i++) {
            String contents = list.get(i).getString();
            if ("APOTH_REMOVE_MARKER".equals(contents)) {
                markIdx1 = i;
            }
            if ("APOTH_REMOVE_MARKER_2".equals(contents)) {
                markIdx2 = i;
                break;
            }
        }
        if (markIdx1 == -1 || markIdx2 == -1) return;
        ListIterator<ITextComponent> it = list.listIterator(markIdx1);
        for (int i = markIdx1; i < markIdx2 + 1; i++) {
            it.next();
            it.remove();
        }
        int flags = getHideFlags(stack);
        if (shouldShowInTooltip(flags, ItemStack.TooltipDisplayFlags.MODIFIERS)) {
            applyModifierTooltips(e.getPlayer(), stack, it::add, e.getFlags());
        }
        MinecraftForge.EVENT_BUS.post(
                new AddAttributeTooltipsEvent(stack, e.getPlayer(), list, it, e.getFlags()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void addAttribComponent(GuiScreenEvent.InitGuiEvent.Post e) {
        if (ALConfig.enableAttributesGui && e.getGui() instanceof InventoryScreen) {
            InventoryScreen scn = (InventoryScreen) e.getGui();
            AttributesGui atrComp = new AttributesGui(scn);
            e.addWidget(atrComp);
            e.addWidget(atrComp.toggleBtn);
            e.addWidget(atrComp.hideUnchangedBtn);
            if (AttributesGui.wasOpen || AttributesGui.swappedFromCurios)
                atrComp.toggleVisibility();
            AttributesGui.swappedFromCurios = false;
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void effectGuiTooltips(GatherEffectScreenTooltipsEvent e) {
        List<IFormattableTextComponent> tooltips = e.getTooltip();
        EffectInstance effectInst = e.getEffectInstance();
        Effect effect = effectInst.getPotion();

        IFormattableTextComponent name = tooltips.get(0);
        IFormattableTextComponent duration = tooltips.remove(1);
        duration = new TranslationTextComponent("(%s)", duration).mergeStyle(TextFormatting.WHITE);

        name.append(ITextComponent.getTextComponentOrEmpty(" ")).append(duration);

        if (AttributesLib.getTooltipFlag().isAdvanced()) {
            name.append(ITextComponent.getTextComponentOrEmpty(" "))
                    .append(
                            new TranslationTextComponent("[%s]", Registry.EFFECTS.getKey(effect))
                                    .mergeStyle(TextFormatting.GRAY));
        }

        String key = effect.getName() + ".desc";
        if (I18n.hasKey(key)) {
            tooltips.add(new TranslationTextComponent(key).mergeStyle(TextFormatting.DARK_GRAY));
        } else if (AttributesLib.getTooltipFlag().isAdvanced()
                && effect.getAttributeModifierMap().isEmpty()) {
            tooltips.add(
                    new TranslationTextComponent(key)
                            .mergeStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
        }

        List<Pair<Attribute, AttributeModifier>> list = Lists.newArrayList();
        Map<Attribute, AttributeModifier> map = effect.getAttributeModifierMap();
        if (!map.isEmpty()) {
            for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                AttributeModifier attributemodifier = entry.getValue();
                AttributeModifier attributemodifier1 =
                        new AttributeModifier(
                                attributemodifier.getName(),
                                effect.getAttributeModifierAmount(
                                        effectInst.getAmplifier(), attributemodifier),
                                attributemodifier.getOperation());
                list.add(new Pair<>(entry.getKey(), attributemodifier1));
            }
        }

        if (!list.isEmpty()) {
            for (Pair<Attribute, AttributeModifier> pair : list) {
                tooltips.add(
                        IFormattableAttribute.toComponent(
                                pair.getFirst(), pair.getSecond(), AttributesLib.getTooltipFlag()));
            }
        }
    }

    @SubscribeEvent
    public void potionTooltips(ItemTooltipEvent e) {
        if (!ALConfig.enablePotionTooltips) return;

        ItemStack stack = e.getItemStack();
        List<ITextComponent> tooltips = e.getToolTip();

        if (stack.getItem() instanceof PotionItem) {
            List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
            if (effects.size() == 1 && tooltips.size() >= 2) {
                Effect effect = effects.get(0).getPotion();
                String key = effect.getName() + ".desc";
                if (I18n.hasKey(key)) {
                    tooltips.add(
                            2,
                            new TranslationTextComponent(key).mergeStyle(TextFormatting.DARK_GRAY));
                } else if (e.getFlags().isAdvanced()
                        && effect.getAttributeModifierMap().isEmpty()) {
                    tooltips.add(
                            2,
                            new TranslationTextComponent(key)
                                    .mergeStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
                }
            }
        }
    }

    public static Multimap<Attribute, AttributeModifier> getSortedModifiers(
            ItemStack stack, EquipmentSlotType slot) {
        Multimap<Attribute, AttributeModifier> unsorted = stack.getAttributeModifiers(slot);
        Multimap<Attribute, AttributeModifier> map = AttributeHelper.sortedMap();
        for (Map.Entry<Attribute, AttributeModifier> ent : unsorted.entries()) {
            if (ent.getKey() != null && ent.getValue() != null)
                map.put(ent.getKey(), ent.getValue());
            else
                AttributesLib.LOGGER.debug(
                        "Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}",
                        stack,
                        ent.getKey(),
                        ent.getValue());
        }
        return map;
    }

    public static void apothCrit(int entityId) {
        Entity entity = Minecraft.getInstance().world.getEntityByID(entityId);

        if (entity != null) {
            Minecraft.getInstance()
                    .particles
                    .addParticleEmitter(entity, ALObjects.Particles.APOTH_CRIT.get());
        }
    }

    private static boolean shouldShowInTooltip(
            int pHideFlags, ItemStack.TooltipDisplayFlags pPart) {
        return (pHideFlags & pPart.func_242397_a()) == 0;
    }

    private static int getHideFlags(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("HideFlags", 99)
                ? stack.getTag().getInt("HideFlags")
                : 0;
    }

    private static void applyModifierTooltips(
            @Nullable PlayerEntity player,
            ItemStack stack,
            Consumer<IFormattableTextComponent> tooltip,
            ITooltipFlag flag) {
        Multimap<Attribute, AttributeModifier> mainhand =
                getSortedModifiers(stack, EquipmentSlotType.MAINHAND);
        Multimap<Attribute, AttributeModifier> offhand =
                getSortedModifiers(stack, EquipmentSlotType.OFFHAND);
        Multimap<Attribute, AttributeModifier> dualHand = AttributeHelper.sortedMap();
        for (Attribute atr : mainhand.keys()) {
            Collection<AttributeModifier> modifMh = mainhand.get(atr);
            Collection<AttributeModifier> modifOh = offhand.get(atr);
            modifMh.stream()
                    .filter(a1 -> modifOh.stream().anyMatch(a2 -> a1.getID().equals(a2.getID())))
                    .forEach(modif -> dualHand.put(atr, modif));
        }

        dualHand.values()
                .forEach(
                        m -> {
                            mainhand.values().remove(m);
                            offhand.values().removeIf(m1 -> m1.getID().equals(m.getID()));
                        });

        Set<UUID> skips = new HashSet<>();
        MinecraftForge.EVENT_BUS.post(
                new GatherSkippedAttributeTooltipsEvent(stack, player, skips, flag));

        applyTextFor(player, stack, tooltip, dualHand, "both_hands", skips, flag);
        applyTextFor(
                player,
                stack,
                tooltip,
                mainhand,
                EquipmentSlotType.MAINHAND.getName(),
                skips,
                flag);
        applyTextFor(
                player, stack, tooltip, offhand, EquipmentSlotType.OFFHAND.getName(), skips, flag);

        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            if (slot.ordinal() < 2) continue;
            Multimap<Attribute, AttributeModifier> modifiers = getSortedModifiers(stack, slot);
            applyTextFor(player, stack, tooltip, modifiers, slot.getName(), skips, flag);
        }
    }

    private static ITextComponent padded(String padding, ITextComponent comp) {
        return new StringTextComponent(padding).append(comp);
    }

    private static IFormattableTextComponent list() {
        return AttributeHelper.list();
    }

    private static class BaseModifier {

        public final AttributeModifier base;
        public final List<AttributeModifier> children;

        public BaseModifier(AttributeModifier base, List<AttributeModifier> children) {
            this.base = base;
            this.children = children;
        }
    }

    private static final UUID FAKE_MERGED_UUID =
            UUID.fromString("a6b0ac71-e435-416e-a991-7623eaa129a4");

    private static void applyTextFor(
            @Nullable PlayerEntity player,
            ItemStack stack,
            Consumer<IFormattableTextComponent> tooltip,
            Multimap<Attribute, AttributeModifier> modifierMap,
            String group,
            Set<UUID> skips,
            ITooltipFlag flag) {
        if (!modifierMap.isEmpty()) {
            modifierMap.values().removeIf(m -> skips.contains(m.getID()));

            tooltip.accept(new StringTextComponent(""));
            tooltip.accept(
                    new TranslationTextComponent("item.modifiers." + group)
                            .mergeStyle(TextFormatting.GRAY));

            if (modifierMap.isEmpty()) return;

            Map<Attribute, BaseModifier> baseModifs = new IdentityHashMap<>();

            modifierMap.forEach(
                    (attr, modif) -> {
                        if (modif.getID().equals(((IFormattableAttribute) attr).getBaseUUID())) {
                            baseModifs.put(attr, new BaseModifier(modif, new ArrayList<>()));
                        }
                    });

            modifierMap.forEach(
                    (attr, modif) -> {
                        BaseModifier base = baseModifs.get(attr);
                        if (base != null && base.base != modif) {
                            base.children.add(modif);
                        }
                    });

            for (Map.Entry<Attribute, BaseModifier> entry : baseModifs.entrySet()) {
                Attribute attr = entry.getKey();
                BaseModifier baseModif = entry.getValue();
                double entityBase = player == null ? 0 : player.getBaseAttributeValue(attr);
                double base = baseModif.base.getAmount() + entityBase;
                final double rawBase = base;
                double amt = base;
                double baseBonus = ((IFormattableAttribute) attr).getBonusBaseValue(stack);
                for (AttributeModifier modif : baseModif.children) {
                    if (modif.getOperation() == AttributeModifier.Operation.ADDITION)
                        base = amt = amt + modif.getAmount();
                    else if (modif.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                        amt += modif.getAmount() * base;
                    else amt *= 1 + modif.getAmount();
                }
                amt += baseBonus;
                boolean isMerged = !baseModif.children.isEmpty() || baseBonus != 0;
                ITextComponent text =
                        IFormattableAttribute.toBaseComponent(
                                attr, amt, entityBase, isMerged, flag);
                tooltip.accept(
                        padded(" ", text)
                                .deepCopy()
                                .mergeStyle(
                                        isMerged
                                                ? TextFormatting.GOLD
                                                : TextFormatting.DARK_GREEN));
                if (Screen.hasShiftDown() && isMerged) {
                    // Display the raw base value, and then all children modifiers.
                    text =
                            IFormattableAttribute.toBaseComponent(
                                    attr, rawBase, entityBase, false, flag);
                    tooltip.accept(
                            list().append(text.deepCopy().mergeStyle(TextFormatting.DARK_GREEN)));
                    for (AttributeModifier modifier : baseModif.children) {
                        tooltip.accept(
                                list().append(
                                                IFormattableAttribute.toComponent(
                                                        attr, modifier, flag)));
                    }
                    if (baseBonus > 0) {
                        ((IFormattableAttribute) attr).addBonusTooltips(stack, tooltip, flag);
                    }
                }
            }

            for (Attribute attr : modifierMap.keySet()) {
                if (baseModifs.containsKey(attr)) continue;
                Collection<AttributeModifier> modifs = modifierMap.get(attr);
                // Initiate merged-tooltip logic if we have more than one modifier for a given
                // attribute.
                if (modifs.size() > 1) {
                    double[] sums = new double[3];
                    boolean[] merged = new boolean[3];
                    Map<AttributeModifier.Operation, List<AttributeModifier>> shiftExpands =
                            new HashMap<>();
                    for (AttributeModifier modifier : modifs) {
                        if (modifier.getAmount() == 0) continue;
                        if (sums[modifier.getOperation().ordinal()] != 0)
                            merged[modifier.getOperation().ordinal()] = true;
                        sums[modifier.getOperation().ordinal()] += modifier.getAmount();
                        shiftExpands
                                .computeIfAbsent(modifier.getOperation(), k -> new LinkedList<>())
                                .add(modifier);
                    }
                    for (AttributeModifier.Operation op : AttributeModifier.Operation.values()) {
                        int i = op.ordinal();
                        if (sums[i] == 0) continue;
                        if (merged[i]) {
                            Color color =
                                    sums[i] < 0 ? Color.fromInt(0xF93131) : Color.fromInt(0x7A7AF9);
                            if (sums[i] < 0) sums[i] *= -1;
                            AttributeModifier fakeModif =
                                    new AttributeModifier(
                                            FAKE_MERGED_UUID,
                                            () -> AttributesLib.MODID + ":merged",
                                            sums[i],
                                            op);
                            ITextComponent comp =
                                    IFormattableAttribute.toComponent(attr, fakeModif, flag);
                            tooltip.accept(
                                    comp.deepCopy().mergeStyle(comp.getStyle().setColor(color)));
                            if (merged[i] && Screen.hasShiftDown()) {
                                shiftExpands
                                        .get(AttributeModifier.Operation.byId(i))
                                        .forEach(
                                                modif ->
                                                        tooltip.accept(
                                                                list().append(
                                                                                IFormattableAttribute
                                                                                        .toComponent(
                                                                                                attr,
                                                                                                modif,
                                                                                                flag))));
                            }
                        } else {
                            AttributeModifier fakeModif =
                                    new AttributeModifier(
                                            FAKE_MERGED_UUID,
                                            () -> AttributesLib.MODID + ":merged",
                                            sums[i],
                                            op);
                            tooltip.accept(
                                    IFormattableAttribute.toComponent(attr, fakeModif, flag));
                        }
                    }
                } else
                    modifs.forEach(
                            m -> {
                                if (m.getAmount() != 0)
                                    tooltip.accept(
                                            IFormattableAttribute.toComponent(attr, m, flag));
                            });
            }
        }
    }

    public static class ApothCritParticle extends CritParticle {
        public ApothCritParticle(
                BasicParticleType type,
                ClientWorld pLevel,
                double pX,
                double pY,
                double pZ,
                double pXSpeed,
                double pYSpeed,
                double pZSpeed) {
            super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            setColor(0.3F, 0.8F, 1F);
        }
    }
}
