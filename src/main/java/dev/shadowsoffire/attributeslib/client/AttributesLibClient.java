package dev.shadowsoffire.attributeslib.client;

import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.attributeslib.api.client.AddAttributeTooltipsEvent;
import dev.shadowsoffire.attributeslib.api.client.GatherEffectScreenTooltipsEvent;
import dev.shadowsoffire.attributeslib.api.client.GatherSkippedAttributeTooltipsEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.particle.ParticleCrit;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
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
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class AttributesLibClient {

    private static final UUID FAKE_MERGED_UUID =
            UUID.fromString("a6b0ac71-e435-416e-a991-7623eaa129a4");
    private static final int HIDE_MODIFIERS = 2;
    private static final List<IAttribute> KNOWN_ATTRIBUTES =
            Arrays.asList(
                    SharedMonsterAttributes.MAX_HEALTH,
                    SharedMonsterAttributes.FOLLOW_RANGE,
                    SharedMonsterAttributes.KNOCKBACK_RESISTANCE,
                    SharedMonsterAttributes.MOVEMENT_SPEED,
                    SharedMonsterAttributes.ATTACK_DAMAGE,
                    SharedMonsterAttributes.ATTACK_SPEED,
                    SharedMonsterAttributes.ARMOR,
                    SharedMonsterAttributes.ARMOR_TOUGHNESS,
                    ALObjects.Attributes.ARMOR_PIERCE.get(),
                    ALObjects.Attributes.ARMOR_SHRED.get(),
                    ALObjects.Attributes.ARROW_DAMAGE.get(),
                    ALObjects.Attributes.ARROW_VELOCITY.get(),
                    ALObjects.Attributes.COLD_DAMAGE.get(),
                    ALObjects.Attributes.CRIT_CHANCE.get(),
                    ALObjects.Attributes.CRIT_DAMAGE.get(),
                    ALObjects.Attributes.CURRENT_HP_DAMAGE.get(),
                    ALObjects.Attributes.DODGE_CHANCE.get(),
                    ALObjects.Attributes.DRAW_SPEED.get(),
                    ALObjects.Attributes.EXPERIENCE_GAINED.get(),
                    ALObjects.Attributes.FIRE_DAMAGE.get(),
                    ALObjects.Attributes.GHOST_HEALTH.get(),
                    ALObjects.Attributes.HEALING_RECEIVED.get(),
                    ALObjects.Attributes.LIFE_STEAL.get(),
                    ALObjects.Attributes.MINING_SPEED.get(),
                    ALObjects.Attributes.OVERHEAL.get(),
                    ALObjects.Attributes.PROT_PIERCE.get(),
                    ALObjects.Attributes.PROT_SHRED.get(),
                    ALObjects.Attributes.ELYTRA_FLIGHT.get(),
                    ALObjects.Attributes.CREATIVE_FLIGHT.get());

    @Nullable private AttributesGui attributesGui;

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tooltips(ItemTooltipEvent e) {
        List<String> tooltips = e.getToolTip();
        int markIdx1 = tooltips.indexOf("APOTH_REMOVE_MARKER");
        int markIdx2 = tooltips.indexOf("APOTH_REMOVE_MARKER_2");
        if (markIdx1 == -1 || markIdx2 == -1 || markIdx2 < markIdx1) return;

        ListIterator<String> it = tooltips.listIterator(markIdx1);
        for (int i = markIdx1; i <= markIdx2; i++) {
            it.next();
            it.remove();
        }

        if (shouldShowInTooltip(getHideFlags(e.getItemStack()))) {
            applyModifierTooltips(e.getEntityPlayer(), e.getItemStack(), it::add, e.getFlags());
        }

        MinecraftForge.EVENT_BUS.post(
                new AddAttributeTooltipsEvent(
                        e.getItemStack(), e.getEntityPlayer(), tooltips, it, e.getFlags()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void addAttribComponent(GuiScreenEvent.InitGuiEvent.Post e) {
        if (!ALConfig.enableAttributesGui || !(e.getGui() instanceof GuiInventory)) {
            this.attributesGui = null;
            return;
        }

        this.attributesGui = new AttributesGui((GuiInventory) e.getGui());
        e.getButtonList().add(this.attributesGui.getToggleButton());
        e.getButtonList().add(this.attributesGui.getHideUnchangedButton());
        if (AttributesGui.wasOpen || AttributesGui.swappedFromCurios) {
            this.attributesGui.toggleVisibility();
        }
        AttributesGui.swappedFromCurios = false;
    }

    @SubscribeEvent
    public void drawAttribComponent(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (!(e.getGui() instanceof GuiInventory) || this.attributesGui == null) return;
        this.attributesGui.render(e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
    }

    @SubscribeEvent
    public void mouseAttribComponent(GuiScreenEvent.MouseInputEvent.Post e) {
        if (!(e.getGui() instanceof GuiInventory) || this.attributesGui == null) return;
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getEventX() * scaled.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY =
                scaled.getScaledHeight()
                        - Mouse.getEventY() * scaled.getScaledHeight()
                                / Minecraft.getMinecraft().displayHeight
                        - 1;
        if (this.attributesGui.handleMouseInput(mouseX, mouseY)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void buttonAttribComponent(GuiScreenEvent.ActionPerformedEvent.Post e) {
        if (!(e.getGui() instanceof GuiInventory) || this.attributesGui == null) return;
        if (this.attributesGui.handles(e.getButton())) {
            this.attributesGui.onButtonPressed(e.getButton());
        }
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

    private static Multimap<IAttribute, AttributeModifier> getSortedModifiers(
            ItemStack stack, EntityEquipmentSlot slot, @Nullable EntityPlayer player) {
        Multimap<IAttribute, AttributeModifier> map = AttributeHelper.sortedMap();
        for (Map.Entry<String, AttributeModifier> ent : stack.getAttributeModifiers(slot).entries()) {
            IAttribute attr = resolveAttribute(ent.getKey(), player);
            AttributeModifier modif = ent.getValue();
            if (attr != null && modif != null) {
                map.put(attr, modif);
            } else {
                AttributesLib.LOGGER.debug(
                        "Detected broken attribute modifier entry on item {}. Attr={}, Modif={}",
                        stack,
                        ent.getKey(),
                        modif);
            }
        }
        return map;
    }

    private static int getHideFlags(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("HideFlags", 99)
                ? stack.getTagCompound().getInteger("HideFlags")
                : 0;
    }

    private static boolean shouldShowInTooltip(int hideFlags) {
        return (hideFlags & HIDE_MODIFIERS) == 0;
    }

    private static void applyModifierTooltips(
            @Nullable EntityPlayer player,
            ItemStack stack,
            Consumer<String> tooltip,
            net.minecraft.client.util.ITooltipFlag flag) {
        Multimap<IAttribute, AttributeModifier> mainhand =
                getSortedModifiers(stack, EntityEquipmentSlot.MAINHAND, player);
        Multimap<IAttribute, AttributeModifier> offhand =
                getSortedModifiers(stack, EntityEquipmentSlot.OFFHAND, player);
        Multimap<IAttribute, AttributeModifier> dualHand = AttributeHelper.sortedMap();

        for (IAttribute attr : new ArrayList<>(mainhand.keySet())) {
            Collection<AttributeModifier> modifMh = mainhand.get(attr);
            Collection<AttributeModifier> modifOh = offhand.get(attr);
            for (AttributeModifier modifier : new ArrayList<>(modifMh)) {
                boolean presentInOffhand =
                        modifOh.stream().anyMatch(other -> modifier.getID().equals(other.getID()));
                if (presentInOffhand) {
                    dualHand.put(attr, modifier);
                }
            }
        }

        dualHand.values()
                .forEach(
                        modifier -> {
                            mainhand.values().remove(modifier);
                            offhand.values()
                                    .removeIf(other -> modifier.getID().equals(other.getID()));
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
                EntityEquipmentSlot.MAINHAND.getName(),
                skips,
                flag);
        applyTextFor(
                player,
                stack,
                tooltip,
                offhand,
                EntityEquipmentSlot.OFFHAND.getName(),
                skips,
                flag);

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) continue;
            Multimap<IAttribute, AttributeModifier> modifiers = getSortedModifiers(stack, slot, player);
            applyTextFor(player, stack, tooltip, modifiers, slot.getName(), skips, flag);
        }
    }

    private static void applyTextFor(
            @Nullable EntityPlayer player,
            ItemStack stack,
            Consumer<String> tooltip,
            Multimap<IAttribute, AttributeModifier> modifierMap,
            String group,
            Set<UUID> skips,
            net.minecraft.client.util.ITooltipFlag flag) {
        if (modifierMap.isEmpty()) return;

        modifierMap.values().removeIf(modifier -> skips.contains(modifier.getID()));
        if (modifierMap.isEmpty()) return;

        tooltip.accept("");
        tooltip.accept(TextFormatting.GRAY + I18n.format("item.modifiers." + group));

        Map<IAttribute, BaseModifier> baseModifs = new IdentityHashMap<>();
        modifierMap.forEach(
                (attr, modif) -> {
                    UUID baseUuid = getBaseUuid(attr);
                    if (baseUuid != null && baseUuid.equals(modif.getID())) {
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

        for (Map.Entry<IAttribute, BaseModifier> entry : baseModifs.entrySet()) {
            IAttribute attr = entry.getKey();
            BaseModifier baseModif = entry.getValue();
            double entityBase = getEntityBase(player, attr);
            double base = baseModif.base.getAmount() + entityBase;
            double rawBase = base;
            double amt = base;
            double baseBonus = getBonusBaseValue(attr, stack);

            for (AttributeModifier modif : baseModif.children) {
                if (modif.getOperation() == 0) {
                    base = amt = amt + modif.getAmount();
                } else if (modif.getOperation() == 1) {
                    amt += modif.getAmount() * base;
                } else {
                    amt *= 1 + modif.getAmount();
                }
            }

            amt += baseBonus;
            boolean merged = !baseModif.children.isEmpty() || baseBonus != 0;
            ITextComponent text = IFormattableAttribute.toBaseComponent(attr, amt, entityBase, merged, flag);
            text.getStyle()
                    .setColor(merged ? TextFormatting.GOLD : TextFormatting.DARK_GREEN);
            tooltip.accept(asString(new TextComponentString(" ").appendSibling(text)));

            if (GuiScreen.isShiftKeyDown() && merged) {
                ITextComponent rawText =
                        IFormattableAttribute.toBaseComponent(attr, rawBase, entityBase, false, flag);
                rawText.getStyle().setColor(TextFormatting.DARK_GREEN);
                tooltip.accept(asString(AttributeHelper.list().appendSibling(rawText)));

                for (AttributeModifier modifier : baseModif.children) {
                    tooltip.accept(
                            asString(
                                    AttributeHelper.list()
                                            .appendSibling(
                                                    IFormattableAttribute.toComponent(
                                                            attr, modifier, flag))));
                }

                if (attr instanceof IFormattableAttribute && baseBonus > 0) {
                    ((IFormattableAttribute) attr)
                            .addBonusTooltips(
                                    stack,
                                    comp ->
                                            tooltip.accept(
                                                    asString(AttributeHelper.list().appendSibling(comp))),
                                    flag);
                }
            }
        }

        for (IAttribute attr : modifierMap.keySet()) {
            if (baseModifs.containsKey(attr)) continue;

            Collection<AttributeModifier> modifs = modifierMap.get(attr);
            if (modifs.size() > 1) {
                double[] sums = new double[3];
                boolean[] merged = new boolean[3];
                Map<Integer, List<AttributeModifier>> shiftExpands = new HashMap<>();

                for (AttributeModifier modifier : modifs) {
                    if (modifier.getAmount() == 0) continue;
                    if (sums[modifier.getOperation()] != 0) {
                        merged[modifier.getOperation()] = true;
                    }
                    sums[modifier.getOperation()] += modifier.getAmount();
                    shiftExpands.computeIfAbsent(modifier.getOperation(), key -> new LinkedList<>())
                            .add(modifier);
                }

                for (int op = 0; op < sums.length; op++) {
                    if (sums[op] == 0) continue;

                    double sum = sums[op];
                    AttributeModifier fakeModif =
                            new AttributeModifier(
                                    FAKE_MERGED_UUID, AttributesLib.MODID + ":merged", sum, op);
                    if (merged[op]) {
                        ITextComponent comp = IFormattableAttribute.toComponent(attr, fakeModif, flag);
                        if (sum < 0) {
                            comp.getStyle().setColor(TextFormatting.RED);
                        } else {
                            comp.getStyle().setColor(TextFormatting.BLUE);
                        }
                        tooltip.accept(asString(comp));
                        if (GuiScreen.isShiftKeyDown()) {
                            for (AttributeModifier modif : shiftExpands.get(op)) {
                                tooltip.accept(
                                        asString(
                                                AttributeHelper.list()
                                                        .appendSibling(
                                                                IFormattableAttribute.toComponent(
                                                                        attr, modif, flag))));
                            }
                        }
                    } else {
                        tooltip.accept(asString(IFormattableAttribute.toComponent(attr, fakeModif, flag)));
                    }
                }
            } else {
                modifs.forEach(
                        modifier -> {
                            if (modifier.getAmount() != 0) {
                                tooltip.accept(
                                        asString(
                                                IFormattableAttribute.toComponent(
                                                        attr, modifier, flag)));
                            }
                        });
            }
        }
    }

    @Nullable
    private static IAttribute resolveAttribute(String key, @Nullable EntityPlayer player) {
        if (player != null) {
            IAttributeInstance inst = player.getAttributeMap().getAttributeInstanceByName(key);
            if (inst != null) {
                return inst.getAttribute();
            }
        }

        for (IAttribute attr : KNOWN_ATTRIBUTES) {
            if (attr != null && key.equals(attr.getName())) {
                return attr;
            }
        }
        return null;
    }

    private static UUID getBaseUuid(IAttribute attr) {
        return attr instanceof IFormattableAttribute ? ((IFormattableAttribute) attr).getBaseUUID() : null;
    }

    private static double getBonusBaseValue(IAttribute attr, ItemStack stack) {
        return attr instanceof IFormattableAttribute
                ? ((IFormattableAttribute) attr).getBonusBaseValue(stack)
                : 0;
    }

    private static double getEntityBase(@Nullable EntityPlayer player, IAttribute attr) {
        if (player == null) return 0;
        IAttributeInstance inst = player.getEntityAttribute(attr);
        return inst == null ? 0 : inst.getBaseValue();
    }

    static List<IAttribute> getKnownAttributes() {
        return KNOWN_ATTRIBUTES;
    }

    static String asString(ITextComponent component) {
        return component.getFormattedText();
    }

    private static class BaseModifier {

        private final AttributeModifier base;
        private final List<AttributeModifier> children;

        private BaseModifier(AttributeModifier base, List<AttributeModifier> children) {
            this.base = base;
            this.children = children;
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
