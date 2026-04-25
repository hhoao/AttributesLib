package dev.shadowsoffire.attributeslib.client;

import static net.minecraft.client.gui.GuiComponent.blit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.attributeslib.impl.BooleanAttribute;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributesGui extends Screen {

    public static final ResourceLocation TEXTURES =
            AttributesLib.loc("textures/gui/attributes_gui.png");
    public static final int ENTRY_HEIGHT = 22;
    public static final int MAX_ENTRIES = 6;
    public static final int WIDTH = 131;
    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_HEIGHT = 256;

    // There's only one player, so we can just happily track if this menu was open via static field.
    // It isn't persistent through sessions, but that's not a huge issue.
    public static boolean wasOpen = false;
    // Similar to the above, we use a static field to record where the scroll bar was.
    protected static float scrollOffset = 0;
    // Ditto.
    protected static boolean hideUnchanged = false;
    protected static boolean swappedFromCurios = false;

    protected final InventoryScreen parent;
    protected final Player player;
    protected final ImageButton toggleBtn;
    protected final ImageButton recipeBookButton;
    protected final HideUnchangedButton hideUnchangedBtn;
    private final MultiBufferSource.BufferSource bufferSource;

    protected int leftPos, topPos;
    protected boolean scrolling;
    protected int startIndex;
    protected List<AttributeInstance> data = new ArrayList<>();
    @Nullable protected AttributeInstance selected = null;
    protected boolean open = false;
    protected long lastRenderTick = -1;

    public AttributesGui(InventoryScreen parent) {
        super(parent.getTitle());
        this.parent = parent;
        this.bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        this.font = Minecraft.getInstance().font;
        this.player = Minecraft.getInstance().player;
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.refreshData();
        this.width = WIDTH;
        this.height = parent.height;
        this.leftPos = parent.getGuiLeft() - WIDTH;
        this.topPos = parent.getGuiTop();
        this.toggleBtn =
                new ImageButton(
                        parent.getGuiLeft() + 63,
                        parent.getGuiTop() + 10,
                        10,
                        10,
                        WIDTH,
                        0,
                        10,
                        TEXTURES,
                        IMAGE_WIDTH,
                        IMAGE_HEIGHT,
                        btn -> {
                            this.toggleVisibility();
                        },
                        new TranslatableComponent("attributeslib.gui.show_attributes")) {
                    @Override
                    public void setFocused(boolean pFocused) {}
                };
        if (this.parent.children().size() > 1) {
            GuiEventListener btn = this.parent.children().get(0);
            this.recipeBookButton = btn instanceof ImageButton imgBtn ? imgBtn : null;
        } else this.recipeBookButton = null;
        this.hideUnchangedBtn = new HideUnchangedButton(0, 0);
    }

    @SuppressWarnings("deprecation")
    public void refreshData() {
        this.data.clear();

        ForgeRegistries.ATTRIBUTES.getValues().stream()
                .map(this.player::getAttribute)
                .filter(Objects::nonNull)
                .filter(
                        ai ->
                                !ALConfig.hiddenAttributes.contains(
                                        Registry.ATTRIBUTE.getKey(ai.getAttribute())))
                .filter(ai -> !hideUnchanged || (ai.getBaseValue() != ai.getValue()))
                .forEach(this.data::add);
        this.data.sort(this::compareAttrs);
        this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
    }

    public void toggleVisibility() {
        this.open = !this.open;
        if (this.open && this.parent.getRecipeBookComponent().isVisible()) {
            this.parent.getRecipeBookComponent().toggleVisibility();
        }
        this.hideUnchangedBtn.visible = this.open;

        int newLeftPos;
        if (this.open && this.parent.width >= 379) {
            newLeftPos = 177 + (this.parent.width - this.parent.imageWidth - 200) / 2;
        } else {
            newLeftPos = (this.parent.width - this.parent.imageWidth) / 2;
        }

        this.parent.leftPos = newLeftPos;
        this.leftPos = this.parent.getGuiLeft() - WIDTH;
        this.topPos = this.parent.getGuiTop();

        if (this.recipeBookButton != null)
            this.recipeBookButton.setPosition(
                    this.parent.getGuiLeft() + 104, this.parent.height / 2 - 22);
        this.hideUnchangedBtn.setPosition(this.leftPos + 7, this.topPos + 151);
    }

    protected int compareAttrs(AttributeInstance a1, AttributeInstance a2) {
        String name = I18n.get(a1.getAttribute().getDescriptionId());
        String name2 = I18n.get(a2.getAttribute().getDescriptionId());
        return name.compareTo(name2);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        if (!this.open) return false;
        if (this.hideUnchangedBtn.visible && this.hideUnchangedBtn.isMouseOver(pMouseX, pMouseY))
            return false;
        return this.isHovering(0, 0, WIDTH, 166, pMouseX, pMouseY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.toggleBtn.x = this.parent.getGuiLeft() + 63;
        this.toggleBtn.y = this.parent.getGuiTop() + 10;
        if (this.parent.getRecipeBookComponent().isVisible()) this.open = false;
        wasOpen = this.open;
        if (!this.open) return;

        this.refreshData();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURES);
        int left = this.leftPos;
        int top = this.topPos;
        blit(poseStack, left, top, 0, 0, WIDTH, 166, IMAGE_HEIGHT, IMAGE_HEIGHT);
        int scrollbarPos = (int) (117 * scrollOffset);
        blit(
                poseStack,
                left + 111,
                top + 16 + scrollbarPos,
                244,
                this.isScrollBarActive() ? 0 : 15,
                12,
                15,
                IMAGE_WIDTH,
                IMAGE_HEIGHT);
        int idx = this.startIndex;
        while (idx < this.startIndex + MAX_ENTRIES && idx < this.data.size()) {
            this.renderEntry(
                    poseStack,
                    this.data.get(idx),
                    this.leftPos + 8,
                    this.topPos + 16 + ENTRY_HEIGHT * (idx - this.startIndex),
                    mouseX,
                    mouseY);
            idx++;
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
        font.draw(
                poseStack,
                new TranslatableComponent("attributeslib.gui.attributes"),
                this.leftPos + 8,
                this.topPos + 5,
                0x404040);
        font.draw(
                poseStack,
                new TextComponent("Hide Unchanged"),
                this.leftPos + 20,
                this.topPos + 152,
                0x404040);
    }

    @SuppressWarnings("deprecation")
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        AttributeInstance inst = this.getHoveredSlot(mouseX, mouseY);
        if (inst != null) {

            boolean isDynamic =
                    Registry.ATTRIBUTE
                            .getKey(inst.getAttribute())
                            .equals(ALObjects.Tags.DYNAMIC_BASE_ATTTE);

            Attribute attr = inst.getAttribute();
            IFormattableAttribute fAttr = (IFormattableAttribute) attr;
            List<Component> list = new ArrayList<>();
            MutableComponent name =
                    new TranslatableComponent(attr.getDescriptionId())
                            .withStyle(
                                    Style.EMPTY
                                            .withColor(ChatFormatting.GOLD)
                                            .withUnderlined(true));

            if (isDynamic) {
                name.append(
                        new TextComponent((" (dynamic)"))
                                .withStyle(
                                        Style.EMPTY
                                                .withColor(ChatFormatting.DARK_GRAY)
                                                .withUnderlined(false)));
            }

            if (AttributesLib.getTooltipFlag().isAdvanced()) {

                Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withUnderlined(false);
                name.append(
                        new TextComponent(" [" + ForgeRegistries.ATTRIBUTES.getKey(attr) + "]")
                                .withStyle(style));
            }

            list.add(name);

            String key = attr.getDescriptionId() + ".desc";

            if (I18n.exists(key)) {
                Component txt =
                        new TranslatableComponent(key)
                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC);
                list.add(txt);
            } else if (AttributesLib.getTooltipFlag().isAdvanced()) {
                Component txt =
                        new TextComponent(key)
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                list.add(txt);
            }

            ChatFormatting color = ChatFormatting.GRAY;
            if (attr instanceof RangedAttribute) {
                if (inst.getValue() > inst.getBaseValue()) {
                    color = ChatFormatting.YELLOW;
                } else if (inst.getValue() < inst.getBaseValue()) {
                    color = ChatFormatting.RED;
                }
            }

            Component valueComp =
                    fAttr.toValueComponent(null, inst.getValue(), AttributesLib.getTooltipFlag())
                            .withStyle(color);
            Component baseComp =
                    fAttr.toValueComponent(
                                    null, inst.getBaseValue(), AttributesLib.getTooltipFlag())
                            .withStyle(ChatFormatting.GRAY);

            if (!isDynamic) {
                list.add(new TextComponent(""));
                list.add(
                        new TranslatableComponent("attributeslib.gui.current", valueComp)
                                .withStyle(ChatFormatting.GRAY));

                Component base =
                        new TranslatableComponent("attributeslib.gui.base", baseComp)
                                .withStyle(ChatFormatting.GRAY);

                if (attr instanceof RangedAttribute ra) {
                    Component min =
                            fAttr.toValueComponent(
                                    null, ra.getMinValue(), AttributesLib.getTooltipFlag());
                    min = new TranslatableComponent("attributeslib.gui.min", min);
                    Component max =
                            fAttr.toValueComponent(
                                    null, ra.getMaxValue(), AttributesLib.getTooltipFlag());
                    max = new TranslatableComponent("attributeslib.gui.max", max);
                    list.add(
                            new TranslatableComponent("%s \u2507 %s \u2507 %s", base, min, max)
                                    .withStyle(ChatFormatting.GRAY));
                } else {
                    list.add(base);
                }
            }

            List<ClientTooltipComponent> finalTooltip = new ArrayList<>(list.size());
            for (Component txt : list) {
                this.addComp(txt, finalTooltip);
            }

            if (inst.getModifiers().stream().anyMatch(modif -> modif.getAmount() != 0)) {
                this.addComp(new TextComponent(""), finalTooltip);
                this.addComp(
                        new TranslatableComponent("attributeslib.gui.modifiers")
                                .withStyle(ChatFormatting.GOLD),
                        finalTooltip);

                Map<UUID, ModifierSource<?>> modifiersToSources = new HashMap<>();

                for (ModifierSourceType<?> type : ModifierSourceType.getTypes()) {
                    type.extract(
                            this.player,
                            (modif, source) -> modifiersToSources.put(modif.getId(), source));
                }

                MutableComponent[] opValues = new MutableComponent[3];
                double[] numericValues = new double[3];

                for (Operation op : Operation.values()) {
                    List<AttributeModifier> modifiers = new ArrayList<>(inst.getModifiers(op));
                    double opValue =
                            modifiers.stream()
                                    .mapToDouble(AttributeModifier::getAmount)
                                    .reduce(
                                            op == Operation.MULTIPLY_TOTAL ? 1 : 0,
                                            (res, elem) ->
                                                    op == Operation.MULTIPLY_TOTAL
                                                            ? res * (1 + elem)
                                                            : res + elem);

                    modifiers.sort(ModifierSourceType.compareBySource(modifiersToSources));
                    for (AttributeModifier modif : modifiers) {
                        if (modif.getAmount() != 0) {
                            Component comp =
                                    fAttr.toComponent(modif, AttributesLib.getTooltipFlag());
                            var src = modifiersToSources.get(modif.getId());
                            finalTooltip.add(
                                    new AttributeModifierComponent(
                                            src, comp, this.font, this.leftPos - 16));
                        }
                    }
                    color = ChatFormatting.GRAY;
                    double threshold = op == Operation.MULTIPLY_TOTAL ? 1.0005 : 0.0005;

                    if (opValue > threshold) {
                        color = ChatFormatting.YELLOW;
                    } else if (opValue < -threshold) {
                        color = ChatFormatting.RED;
                    }
                    Component valueComp2 =
                            fAttr.toValueComponent(op, opValue, AttributesLib.getTooltipFlag())
                                    .withStyle(color);
                    MutableComponent comp =
                            new TranslatableComponent(
                                            "attributeslib.gui."
                                                    + op.name().toLowerCase(Locale.ROOT),
                                            valueComp2)
                                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                    opValues[op.ordinal()] = comp;
                    numericValues[op.ordinal()] = opValue;
                }

                this.addComp(new TextComponent(""), finalTooltip);
                this.addComp(
                        new TextComponent("Modifier Formula").withStyle(ChatFormatting.GOLD),
                        finalTooltip);

                Component base =
                        isDynamic
                                ? new TranslatableComponent("attributeslib.gui.formula.base")
                                : baseComp;
                Component value =
                        isDynamic
                                ? new TranslatableComponent("attributeslib.gui.formula.value")
                                : valueComp;

                Component formula = buildFormula(base, value, numericValues);
                this.addComp(formula, finalTooltip);
            } else if (isDynamic) {
                this.addComp(new TextComponent(""), finalTooltip);
                this.addComp(
                        new TranslatableComponent("attributeslib.gui.no_modifiers")
                                .withStyle(ChatFormatting.GOLD),
                        finalTooltip);
            }

            renderTooltipInternal(
                    poseStack,
                    finalTooltip,
                    this.leftPos
                            - 16
                            - finalTooltip.stream()
                                    .map(c -> c.getWidth(this.font))
                                    .max(Integer::compare)
                                    .get(),
                    mouseY);
        }
    }

    private void addComp(Component comp, List<ClientTooltipComponent> finalTooltip) {
        if (Objects.equals(comp, new TextComponent(""))) {
            finalTooltip.add(ClientTooltipComponent.create(comp.getVisualOrderText()));
        } else {
            for (FormattedText fTxt :
                    this.font.getSplitter().splitLines(comp, this.leftPos - 16, comp.getStyle())) {
                finalTooltip.add(
                        ClientTooltipComponent.create(Language.getInstance().getVisualOrder(fTxt)));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void renderEntry(
            PoseStack poseStack, AttributeInstance inst, int x, int y, int mouseX, int mouseY) {
        boolean hover = this.getHoveredSlot(mouseX, mouseY) == inst;
        RenderSystem.setShaderTexture(0, TEXTURES);
        blit(
                poseStack,
                x,
                y,
                142,
                hover ? ENTRY_HEIGHT : 0,
                100,
                ENTRY_HEIGHT,
                IMAGE_WIDTH,
                IMAGE_HEIGHT);

        Component txt = new TranslatableComponent(inst.getAttribute().getDescriptionId());
        int splitWidth = 60;
        List<FormattedCharSequence> lines = this.font.split(txt, splitWidth);
        // We can only actually display two lines here, but we need to forcibly create two lines and
        // then scale down.
        while (lines.size() > 2) {
            splitWidth += 10;
            lines = this.font.split(txt, splitWidth);
        }

        poseStack.pushPose();
        float scale = 1;
        int maxWidth = lines.stream().map(this.font::width).max(Integer::compareTo).get();
        if (maxWidth > 66) {
            scale = 66F / maxWidth;
            poseStack.scale(scale, scale, 1);
        }

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            float width = this.font.width(line) * scale;
            float lineX = (x + 1 + (68 - width) / 2) / scale;
            float lineY = (y + (lines.size() == 1 ? 7 : 2) + i * 10) / scale;
            font.draw(poseStack, line, lineX, lineY, 0x404040);
        }
        poseStack.popPose();
        poseStack.pushPose();

        var attr = (IFormattableAttribute) inst.getAttribute();
        MutableComponent value =
                attr.toValueComponent(null, inst.getValue(), TooltipFlag.Default.NORMAL);

        if (Registry.ATTRIBUTE
                .getKey(inst.getAttribute())
                .equals(ALObjects.Tags.DYNAMIC_BASE_ATTTE)) {
            value = new TextComponent("\uFFFD");
        }

        scale = 1;
        if (this.font.width(value) > 27) {
            scale = 27F / this.font.width(value);
            poseStack.scale(scale, scale, 1);
        }

        int color = 0xFFFFFF;
        if (attr instanceof RangedAttribute) {
            if (inst.getValue() > inst.getBaseValue()) {
                color = 0x55DD55;
            } else if (inst.getValue() < inst.getBaseValue()) {
                color = 0xFF6060;
            }
        } else if (attr instanceof BooleanAttribute && inst.getValue() > 0) {
            color = 0x55DD55;
        }
        font.drawShadow(
                poseStack,
                value,
                (int) ((x + 72 + (27 - this.font.width(value) * scale) / 2) / scale),
                (int) ((y + 7) / scale),
                color);

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!this.open || !this.isScrollBarActive()) return false;
        this.scrolling = false;
        int left = this.leftPos + 111;
        int top = this.topPos + 15;
        if (pMouseX >= left && pMouseX < left + 12 && pMouseY >= top && pMouseY < top + 155) {
            this.scrolling = true;
            int i = this.topPos + 15;
            int j = i + 138;
            scrollOffset = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(
            double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (!this.open) return false;
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 15;
            int j = i + 138;
            scrollOffset = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!this.open) return false;
        if (this.isScrollBarActive()) {
            int i = this.getOffScreenRows();
            scrollOffset = (float) (scrollOffset - pDelta / i);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (scrollOffset * i + 0.5D);
            return true;
        }
        return false;
    }

    private boolean isScrollBarActive() {
        return this.data.size() > MAX_ENTRIES;
    }

    protected int getOffScreenRows() {
        return Math.max(0, this.data.size() - MAX_ENTRIES);
    }

    @Nullable public AttributeInstance getHoveredSlot(int mouseX, int mouseY) {
        for (int i = 0; i < MAX_ENTRIES; i++) {
            if (this.startIndex + i < this.data.size()) {
                if (this.isHovering(8, 14 + ENTRY_HEIGHT * i, 100, ENTRY_HEIGHT, mouseX, mouseY))
                    return this.data.get(this.startIndex + i);
            }
        }
        return null;
    }

    protected boolean isHovering(
            int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= pX - 1
                && pMouseX < pX + pWidth + 1
                && pMouseY >= pY - 1
                && pMouseY < pY + pHeight + 1;
    }

    private static DecimalFormat f = ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

    public static String format(int n) {
        int log = (int) StrictMath.log10(n);
        if (log <= 4) return String.valueOf(n);
        if (log == 5) return f.format(n / 1000D) + "K";
        if (log <= 8) return f.format(n / 1000000D) + "M";
        else return f.format(n / 1000000000D) + "B";
    }

    /**
     * Builds a component containing the mathematical representation of the attribute calculations.
     *
     * @param base A component of the base value. May be a string if the attribute is dynamic.
     * @param value A component of the final value. May be a string if the attribute is dynamic.
     * @param numericValues The modifier totals, in operation ordinal order (add, mulBase, mulTotal)
     * @return A component holding the formula with colors already applied.
     */
    public static Component buildFormula(Component base, Component value, double[] numericValues) {
        double add = numericValues[0];
        double mulBase = numericValues[1];
        double mulTotal = numericValues[2];

        boolean isAddNeg = add < 0;
        boolean isMulNeg = mulBase < 0;

        String addSym = isAddNeg ? "-" : "+";
        add = Math.abs(add);

        String mulBaseSym = isMulNeg ? "-" : "+";
        mulBase = Math.abs(mulBase);

        String addStr = f.format(add);
        String mulBaseStr = f.format(mulBase);
        String mulTotalStr = f.format(mulTotal);

        String formula = "%2$s";

        if (add != 0) {
            ChatFormatting color = isAddNeg ? ChatFormatting.RED : ChatFormatting.YELLOW;
            formula = formula + " " + colored(addSym + " " + addStr, color);
        }

        if (mulBase != 0) {
            String withParens = add == 0 ? formula : "(%s)".formatted(formula);
            ChatFormatting color = isMulNeg ? ChatFormatting.RED : ChatFormatting.YELLOW;
            formula =
                    withParens
                            + " "
                            + colored(mulBaseSym + " " + mulBaseStr + " * ", color)
                            + withParens;
        }

        if (mulTotal != 1) {
            String withParens = add == 0 && mulBase == 0 ? formula : "(%s)".formatted(formula);
            ChatFormatting color = mulTotal < 1 ? ChatFormatting.RED : ChatFormatting.YELLOW;
            formula = colored(mulTotalStr + " * ", color) + withParens;
        }

        return new TranslatableComponent("%1$s = " + formula, value, base)
                .withStyle(ChatFormatting.GRAY);
    }

    /**
     * Colors a string using legacy formatting codes. Terminates the string with {@link
     * ChatFormatting#RESET}.
     */
    private static String colored(String str, ChatFormatting color) {
        return ""
                + ChatFormatting.PREFIX_CODE
                + color.getChar()
                + str
                + ChatFormatting.PREFIX_CODE
                + ChatFormatting.RESET.getChar();
    }

    public class HideUnchangedButton extends ImageButton {
        public HideUnchangedButton(int pX, int pY) {
            super(
                    pX,
                    pY,
                    10,
                    10,
                    131,
                    20,
                    10,
                    TEXTURES,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT,
                    null,
                    new TextComponent("Hide Unchanged Attributes"));
            this.visible = false;
        }

        @Override
        public void onPress() {
            hideUnchanged = !hideUnchanged;
        }

        @Override
        public void renderButton(PoseStack stack, int pMouseX, int pMouseY, float pPartialTick) {
            int u = 131, v = 20;
            int vOffset = hideUnchanged ? 0 : 10;
            if (this.isHovered) {
                vOffset += 20;
            }

            RenderSystem.enableDepthTest();
            stack.pushPose();
            stack.translate(0, 0, 100);
            blit(stack, this.x, this.y, u, v + vOffset, 10, 10, IMAGE_WIDTH, IMAGE_HEIGHT);
            stack.popPose();
        }
    }
}
