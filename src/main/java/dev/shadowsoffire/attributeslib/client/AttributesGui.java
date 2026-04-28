package dev.shadowsoffire.attributeslib.client;

import dev.shadowsoffire.attributeslib.ALConfig;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.attributeslib.impl.BooleanAttribute;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;

public class AttributesGui {

    public static final ResourceLocation TEXTURES =
            AttributesLib.loc("textures/gui/attributes_gui.png");
    public static final int ENTRY_HEIGHT = 22;
    public static final int MAX_ENTRIES = 6;
    public static final int WIDTH = 131;
    public static final int HEIGHT = 166;
    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_HEIGHT = 256;
    public static final int TOGGLE_BUTTON_ID = 0xA77001;
    public static final int HIDE_UNCHANGED_BUTTON_ID = 0xA77002;

    private static Field GUI_LEFT;
    private static Field GUI_TOP;

    public static boolean wasOpen = false;
    protected static float scrollOffset = 0;
    protected static boolean hideUnchanged = false;
    protected static boolean swappedFromCurios = false;

    private final GuiInventory parent;
    private final EntityPlayerSP player;
    private final Minecraft mc;
    private final FontRenderer font;
    private final ToggleButton toggleBtn;
    private final GuiButton hideUnchangedBtn;
    private final List<IAttributeInstance> data = new ArrayList<>();

    private int leftPos;
    private int topPos;
    private int startIndex;
    private boolean open;
    private boolean scrolling;
    private boolean lastLeftDown;

    public AttributesGui(GuiInventory parent) {
        this.parent = parent;
        this.mc = Minecraft.getMinecraft();
        this.player = this.mc.player;
        this.font = this.mc.fontRenderer;
        this.toggleBtn = new ToggleButton(TOGGLE_BUTTON_ID, 0, 0);
        this.hideUnchangedBtn = new HideUnchangedButton(HIDE_UNCHANGED_BUTTON_ID, 0, 0);
        this.open = wasOpen;
        this.hideUnchangedBtn.visible = this.open;
        this.syncLayout();
        this.refreshData();
    }

    public GuiButton getToggleButton() {
        return this.toggleBtn;
    }

    public GuiButton getHideUnchangedButton() {
        return this.hideUnchangedBtn;
    }

    public boolean handles(GuiButton button) {
        return button != null
                && (button.id == TOGGLE_BUTTON_ID || button.id == HIDE_UNCHANGED_BUTTON_ID);
    }

    public void onButtonPressed(GuiButton button) {
        if (button.id == TOGGLE_BUTTON_ID) {
            this.toggleVisibility();
        } else if (button.id == HIDE_UNCHANGED_BUTTON_ID) {
            hideUnchanged = !hideUnchanged;
            this.refreshData();
            this.syncLayout();
        }
    }

    public boolean isOpen() {
        return this.open;
    }

    public void toggleVisibility() {
        this.setOpen(!this.open);
    }

    /**
     * 1.12.2 的 {@link GuiInventory} 自带 Recipe Book，它也维护自己的 {@code guiLeft}
     * 偏移（通过 {@code GuiRecipeBook.updateScreenPosition}）。之前的实现在这里强写
     * {@code guiLeft} 会和 Recipe Book 的布局直接打架（表现：切配方书或切属性按钮
     * 之后库存整体跑位）。所以这里只改变打开状态、不动 {@code guiLeft}，属性面板
     * 就贴着 {@code guiLeft - WIDTH} 画，原版的布局留给原版自己管。
     */
    public void setOpen(boolean open) {
        this.open = open;
        wasOpen = this.open;
        this.hideUnchangedBtn.visible = this.open;
        this.syncLayout();
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        this.syncLayout();
        this.pollMouseInput(mouseX, mouseY);
        if (!this.open || this.player == null) return;

        this.refreshData();

        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(TEXTURES);
        Gui.drawModalRectWithCustomSizedTexture(
                this.leftPos, this.topPos, 0, 0, WIDTH, HEIGHT, 256, 256);

        this.font.drawString(
                I18n.format("attributeslib.gui.attributes"),
                this.leftPos + 8,
                this.topPos + 6,
                0x404040);

        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(TEXTURES);
        int scrollbarPos = this.getMaxRows() == 0 ? 0 : (int) (117 * scrollOffset);
        Gui.drawModalRectWithCustomSizedTexture(
                this.leftPos + 111,
                this.topPos + 16 + scrollbarPos,
                244,
                this.getMaxRows() > 0 ? 0 : 15,
                12,
                15,
                256,
                256);

        int idx = this.startIndex;
        while (idx < this.startIndex + MAX_ENTRIES && idx < this.data.size()) {
            this.renderEntry(
                    this.data.get(idx),
                    this.leftPos + 8,
                    this.topPos + 16 + ENTRY_HEIGHT * (idx - this.startIndex),
                    mouseX,
                    mouseY);
            idx++;
        }

        IAttributeInstance hovered = this.getHoveredEntry(mouseX, mouseY);
        if (hovered != null) {
            GuiUtils.drawHoveringText(
                    this.buildTooltip(hovered),
                    mouseX,
                    mouseY,
                    this.parent.width,
                    this.parent.height,
                    260,
                    this.font);
        }
    }

    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }

    private void pollMouseInput(int mouseX, int mouseY) {
        if (!this.open) {
            this.scrolling = false;
            this.lastLeftDown = false;
            return;
        }

        int maxRows = this.getMaxRows();
        int wheel = Mouse.getDWheel();
        if (wheel != 0 && this.isMouseOver(mouseX, mouseY) && maxRows > 0) {
            if (wheel < 0) {
                scrollOffset = Math.min(1.0F, scrollOffset + 1.0F / maxRows);
            } else {
                scrollOffset = Math.max(0.0F, scrollOffset - 1.0F / maxRows);
            }
            this.startIndex = Math.round(scrollOffset * maxRows);
        }

        boolean leftDown = Mouse.isButtonDown(0);
        if (!leftDown) {
            this.scrolling = false;
            this.lastLeftDown = false;
            return;
        }

        if (!this.lastLeftDown && maxRows > 0) {
            int scrollLeft = this.leftPos + 111;
            int scrollTop = this.topPos + 15;
            if (mouseX >= scrollLeft
                    && mouseX < scrollLeft + 12
                    && mouseY >= scrollTop
                    && mouseY < scrollTop + 155) {
                this.scrolling = true;
                this.applyScrollFromMouse(mouseY, maxRows);
            }
        } else if (this.scrolling && maxRows > 0) {
            this.applyScrollFromMouse(mouseY, maxRows);
        }

        this.lastLeftDown = true;
    }

    private void applyScrollFromMouse(int mouseY, int maxRows) {
        int scrollTop = this.topPos + 15;
        int scrollBottom = scrollTop + 138;
        float range = scrollBottom - scrollTop - 15.0F;
        if (range > 0) {
            scrollOffset =
                    MathHelper.clamp(((float) mouseY - scrollTop - 7.5F) / range, 0.0F, 1.0F);
            this.startIndex = Math.round(scrollOffset * maxRows);
        }
    }

    private void renderEntry(IAttributeInstance inst, int x, int y, int mouseX, int mouseY) {
        boolean hovered = this.getHoveredEntry(mouseX, mouseY) == inst;
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(TEXTURES);
        Gui.drawModalRectWithCustomSizedTexture(
                x,
                y,
                142,
                hovered ? ENTRY_HEIGHT : 0,
                100,
                ENTRY_HEIGHT,
                IMAGE_WIDTH,
                IMAGE_HEIGHT);

        IAttribute attr = inst.getAttribute();
        String name = translateAttr(attr.getName());
        List<String> lines = this.font.listFormattedStringToWidth(name, 60);
        int splitWidth = 60;
        while (lines.size() > 2) {
            splitWidth += 10;
            lines = this.font.listFormattedStringToWidth(name, splitWidth);
        }

        float nameScale = 1F;
        int maxNameWidth = 0;
        for (String line : lines) {
            maxNameWidth = Math.max(maxNameWidth, this.font.getStringWidth(line));
        }
        if (maxNameWidth > 66) {
            nameScale = 66F / maxNameWidth;
        }

        GlStateManager.pushMatrix();
        if (nameScale != 1F) GlStateManager.scale(nameScale, nameScale, 1F);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float width = this.font.getStringWidth(line) * nameScale;
            float lineX = (x + 1 + (68 - width) / 2F) / nameScale;
            float lineY = (y + (lines.size() == 1 ? 7 : 2) + i * 10) / nameScale;
            this.font.drawString(line, (int) lineX, (int) lineY, 0x404040);
        }
        GlStateManager.popMatrix();

        String value =
                AttributesLibClient.asString(
                        IFormattableAttribute.toValueComponent(
                                attr, null, inst.getAttributeValue(), AttributesLib.getTooltipFlag()));

        if (isDynamic(attr)) {
            value = "\uFFFD";
        }

        int color = 0xFFFFFF;
        if (attr instanceof RangedAttribute) {
            if (inst.getAttributeValue() > inst.getBaseValue()) {
                color = 0x55DD55;
            } else if (inst.getAttributeValue() < inst.getBaseValue()) {
                color = 0xFF6060;
            }
        } else if (attr instanceof BooleanAttribute && inst.getAttributeValue() > 0) {
            color = 0x55DD55;
        }

        int valueWidth = this.font.getStringWidth(value);
        float valueScale = 1F;
        if (valueWidth > 27) {
            valueScale = 27F / valueWidth;
        }

        GlStateManager.pushMatrix();
        if (valueScale != 1F) GlStateManager.scale(valueScale, valueScale, 1F);
        int scaledValueWidth = (int) (valueWidth * valueScale);
        int valueX = (int) ((x + 72 + (27 - scaledValueWidth) / 2F) / valueScale);
        int valueY = (int) ((y + 7) / valueScale);
        this.font.drawStringWithShadow(value, valueX, valueY, color);
        GlStateManager.popMatrix();
    }

    @Nullable
    private IAttributeInstance getHoveredEntry(int mouseX, int mouseY) {
        if (!this.open) return null;
        for (int i = 0; i < MAX_ENTRIES; i++) {
            int dataIdx = this.startIndex + i;
            if (dataIdx >= this.data.size()) return null;
            int x = this.leftPos + 8;
            int y = this.topPos + 16 + ENTRY_HEIGHT * i;
            if (mouseX >= x - 2 && mouseX < x + 100 && mouseY >= y - 1 && mouseY < y + 20) {
                return this.data.get(dataIdx);
            }
        }
        return null;
    }

    private List<String> buildTooltip(IAttributeInstance inst) {
        List<String> tooltip = new ArrayList<>();
        IAttribute attr = inst.getAttribute();
        boolean isDynamic = isDynamic(attr);

        String header =
                TextFormatting.GOLD.toString()
                        + TextFormatting.UNDERLINE
                        + translateAttr(attr.getName());
        if (isDynamic) {
            header +=
                    TextFormatting.RESET.toString()
                            + TextFormatting.DARK_GRAY
                            + " (dynamic)";
        }
        if (AttributesLib.getTooltipFlag().isAdvanced()) {
            header +=
                    TextFormatting.RESET.toString()
                            + TextFormatting.GRAY
                            + " ["
                            + attr.getName()
                            + "]";
        }
        tooltip.add(header);

        String descPrefixed = "attribute.name." + attr.getName() + ".desc";
        String descBare = attr.getName() + ".desc";
        String descKey = I18n.hasKey(descPrefixed) ? descPrefixed : descBare;
        if (I18n.hasKey(descKey)) {
            tooltip.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + I18n.format(descKey));
        } else if (AttributesLib.getTooltipFlag().isAdvanced()) {
            tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + descBare);
        }

        TextFormatting valueColor = TextFormatting.GRAY;
        if (attr instanceof RangedAttribute) {
            if (inst.getAttributeValue() > inst.getBaseValue()) {
                valueColor = TextFormatting.YELLOW;
            } else if (inst.getAttributeValue() < inst.getBaseValue()) {
                valueColor = TextFormatting.RED;
            }
        }
        String valueStr =
                valueColor
                        + AttributesLibClient.asString(
                                IFormattableAttribute.toValueComponent(
                                        attr,
                                        null,
                                        inst.getAttributeValue(),
                                        AttributesLib.getTooltipFlag()))
                        + TextFormatting.RESET
                        + TextFormatting.GRAY;
        String baseStr =
                AttributesLibClient.asString(
                        IFormattableAttribute.toValueComponent(
                                attr,
                                null,
                                inst.getBaseValue(),
                                AttributesLib.getTooltipFlag()));

        if (!isDynamic) {
            tooltip.add("");
            tooltip.add(TextFormatting.GRAY + I18n.format("attributeslib.gui.current", valueStr));

            String baseLine = I18n.format("attributeslib.gui.base", baseStr);
            if (attr instanceof RangedAttribute) {
                RangedAttribute ranged = (RangedAttribute) attr;
                String min =
                        I18n.format(
                                "attributeslib.gui.min",
                                AttributesLibClient.asString(
                                        IFormattableAttribute.toValueComponent(
                                                attr,
                                                null,
                                                ranged.minimumValue,
                                                AttributesLib.getTooltipFlag())));
                String max =
                        I18n.format(
                                "attributeslib.gui.max",
                                AttributesLibClient.asString(
                                        IFormattableAttribute.toValueComponent(
                                                attr,
                                                null,
                                                ranged.maximumValue,
                                                AttributesLib.getTooltipFlag())));
                tooltip.add(TextFormatting.GRAY + baseLine + " \u2507 " + min + " \u2507 " + max);
            } else {
                tooltip.add(TextFormatting.GRAY + baseLine);
            }
        }

        List<AttributeModifier> modifiers = new ArrayList<>(inst.getModifiers());
        boolean hasAny = modifiers.stream().anyMatch(modifier -> modifier.getAmount() != 0);
        if (!hasAny) {
            if (isDynamic) {
                tooltip.add("");
                tooltip.add(
                        TextFormatting.GOLD + I18n.format("attributeslib.gui.no_modifiers"));
            }
            return tooltip;
        }

        tooltip.add("");
        tooltip.add(TextFormatting.GOLD + I18n.format("attributeslib.gui.modifiers"));

        Map<UUID, ModifierSource<?>> sources = new HashMap<>();
        for (ModifierSourceType type : ModifierSourceType.getTypes()) {
            type.extract(this.player, (modifier, source) -> sources.put(modifier.getID(), source));
        }
        modifiers.sort(ModifierSourceType.compareBySource(sources));

        double[] opValues = new double[3];
        opValues[IFormattableAttribute.OP_MULTIPLY_TOTAL] = 1.0D;

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getAmount() == 0) continue;
            int op = modifier.getOperation();
            if (op == IFormattableAttribute.OP_MULTIPLY_TOTAL) {
                opValues[op] *= 1.0D + modifier.getAmount();
            } else {
                opValues[op] += modifier.getAmount();
            }

            ModifierSource<?> source = sources.get(modifier.getID());
            AttributeModifierComponent comp =
                    new AttributeModifierComponent(
                            source,
                            IFormattableAttribute.toComponent(
                                    attr, modifier, AttributesLib.getTooltipFlag()),
                            this.font,
                            240);
            tooltip.addAll(comp.getLines());
        }

        tooltip.add("");
        tooltip.add(TextFormatting.GOLD + I18n.format("attributeslib.gui.formula_header"));

        for (int op = 0; op < 3; op++) {
            boolean isMul = op == IFormattableAttribute.OP_MULTIPLY_TOTAL;
            double opValue = opValues[op];
            double delta = isMul ? opValue - 1.0D : opValue;
            if (Math.abs(delta) < 0.0005D) continue;

            TextFormatting color = TextFormatting.GRAY;
            if (delta > 0.0005D) color = TextFormatting.YELLOW;
            else if (delta < -0.0005D) color = TextFormatting.RED;

            String key = "attributeslib.gui." + opName(op).toLowerCase(Locale.ROOT);
            String valueComp =
                    color
                            + AttributesLibClient.asString(
                                    IFormattableAttribute.toValueComponent(
                                            attr, op, opValue, AttributesLib.getTooltipFlag()))
                            + TextFormatting.RESET
                            + TextFormatting.GRAY
                            + TextFormatting.ITALIC;
            tooltip.add(
                    TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format(key, valueComp));
        }

        String baseFormulaArg =
                isDynamic
                        ? I18n.format("attributeslib.gui.formula.base")
                        : AttributesLibClient.asString(
                                IFormattableAttribute.toValueComponent(
                                        attr,
                                        null,
                                        inst.getBaseValue(),
                                        AttributesLib.getTooltipFlag()));
        String valueFormulaArg =
                isDynamic
                        ? I18n.format("attributeslib.gui.formula.value")
                        : AttributesLibClient.asString(
                                IFormattableAttribute.toValueComponent(
                                        attr,
                                        null,
                                        inst.getAttributeValue(),
                                        AttributesLib.getTooltipFlag()));
        String formula = buildFormulaString(baseFormulaArg, valueFormulaArg, opValues);
        tooltip.add(TextFormatting.GRAY + formula);

        return tooltip;
    }

    /**
     * 1.12.2 下 {@link IAttribute#getName()} 只返回 {@code generic.armor} /
     * {@code forge.swimSpeed} / {@code attributeslib.armor_pierce} 这样的裸 key；
     * vanilla + Forge 本身的 lang 约定是加 {@code attribute.name.} 前缀
     * （比如 {@code attribute.name.generic.armor}）。
     * 先查带前缀的 key，命中就用翻译；否则 fallback 到裸 key（mod 自己的 lang 条目
     * 写的就是裸 key 形式）；都没命中就直接返回裸 key，别显示 "attribute.name.xxx"
     * 这种半成品。
     */
    private static String translateAttr(String key) {
        String prefixed = "attribute.name." + key;
        if (I18n.hasKey(prefixed)) return I18n.format(prefixed);
        if (I18n.hasKey(key)) return I18n.format(key);
        return key;
    }

    private static boolean isDynamic(IAttribute attr) {
        String name = attr.getName();
        if (!name.startsWith(AttributesLib.MODID + ".")) return false;
        ResourceLocation key =
                AttributesLib.loc(name.substring((AttributesLib.MODID + ".").length()));
        return key.equals(ALObjects.Tags.DYNAMIC_BASE_ATTTE);
    }

    private static String opName(int op) {
        switch (op) {
            case IFormattableAttribute.OP_ADDITION:
                return "addition";
            case IFormattableAttribute.OP_MULTIPLY_BASE:
                return "multiply_base";
            case IFormattableAttribute.OP_MULTIPLY_TOTAL:
                return "multiply_total";
            default:
                return "unknown";
        }
    }

    private static final DecimalFormat FMT = ItemStack.DECIMALFORMAT;

    private static String buildFormulaString(String base, String value, double[] numericValues) {
        double add = numericValues[0];
        double mulBase = numericValues[1];
        double mulTotal = numericValues[2];

        boolean isAddNeg = add < 0;
        boolean isMulNeg = mulBase < 0;
        String addSym = isAddNeg ? "-" : "+";
        String mulBaseSym = isMulNeg ? "-" : "+";
        double absAdd = Math.abs(add);
        double absMulBase = Math.abs(mulBase);

        String formula = "%2$s";
        if (absAdd != 0) {
            TextFormatting color = isAddNeg ? TextFormatting.RED : TextFormatting.YELLOW;
            formula = formula + " " + color + addSym + " " + FMT.format(absAdd) + TextFormatting.GRAY;
        }
        if (absMulBase != 0) {
            String withParens = absAdd == 0 ? formula : "(" + formula + ")";
            TextFormatting color = isMulNeg ? TextFormatting.RED : TextFormatting.YELLOW;
            formula =
                    withParens
                            + " "
                            + color
                            + mulBaseSym
                            + " "
                            + FMT.format(absMulBase)
                            + " * "
                            + TextFormatting.GRAY
                            + withParens;
        }
        if (Math.abs(mulTotal - 1.0D) > 0.0005D) {
            String withParens = (absAdd == 0 && absMulBase == 0) ? formula : "(" + formula + ")";
            TextFormatting color = mulTotal < 1.0D ? TextFormatting.RED : TextFormatting.YELLOW;
            formula =
                    color
                            + FMT.format(mulTotal)
                            + " * "
                            + TextFormatting.GRAY
                            + withParens;
        }
        String rendered = String.format("%1$s = " + formula, value, base);
        return rendered;
    }

    private void refreshData() {
        this.data.clear();
        if (this.player == null) return;

        Collection<IAttributeInstance> all = this.player.getAttributeMap().getAllAttributes();
        for (IAttributeInstance inst : all) {
            IAttribute attr = inst.getAttribute();
            if (attr == null || this.isHidden(attr)) continue;
            if (hideUnchanged && inst.getBaseValue() == inst.getAttributeValue()) continue;
            this.data.add(inst);
        }

        this.data.sort(
                Comparator.comparing(
                        inst -> translateAttr(inst.getAttribute().getName()),
                        String.CASE_INSENSITIVE_ORDER));
        this.startIndex = (int) (scrollOffset * this.getMaxRows() + 0.5D);
        this.startIndex = Math.min(this.startIndex, this.getMaxRows());
        this.startIndex = Math.max(this.startIndex, 0);
    }

    private boolean isHidden(IAttribute attr) {
        String name = attr.getName();
        if (name.startsWith(AttributesLib.MODID + ".")) {
            ResourceLocation key =
                    AttributesLib.loc(name.substring((AttributesLib.MODID + ".").length()));
            return ALConfig.hiddenAttributes.contains(key);
        }
        return false;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.leftPos
                && mouseX < this.leftPos + WIDTH
                && mouseY >= this.topPos
                && mouseY < this.topPos + HEIGHT;
    }

    private int getMaxRows() {
        return Math.max(0, this.data.size() - MAX_ENTRIES);
    }

    private void syncLayout() {
        this.leftPos = readGuiInt("guiLeft") - WIDTH;
        this.topPos = readGuiInt("guiTop");
        this.toggleBtn.x = readGuiInt("guiLeft") + 63;
        this.toggleBtn.y = readGuiInt("guiTop") + 10;
        this.hideUnchangedBtn.x = this.leftPos + 7;
        this.hideUnchangedBtn.y = this.topPos + 151;
        this.hideUnchangedBtn.visible = this.open;
    }

    private int readGuiInt(String name) {
        try {
            return accessField(name).getInt(this.parent);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to access GuiContainer layout field " + name, ex);
        }
    }

    private void writeGuiInt(String name, int value) {
        try {
            accessField(name).setInt(this.parent, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to write GuiContainer layout field " + name, ex);
        }
    }

    private static Field accessField(String name) throws ReflectiveOperationException {
        if ("guiLeft".equals(name)) {
            if (GUI_LEFT == null) {
                GUI_LEFT = resolveField("guiLeft", "field_147003_i");
            }
            return GUI_LEFT;
        }
        if (GUI_TOP == null) {
            GUI_TOP = resolveField("guiTop", "field_147009_r");
        }
        return GUI_TOP;
    }

    private static Field resolveField(String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                Field field = GuiContainer.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ex) {
                // Try the next name variant.
            }
        }
        throw new NoSuchFieldException(String.join(", ", names));
    }

    public static class ToggleButton extends GuiButton {

        public ToggleButton(int id, int x, int y) {
            super(id, x, y, 10, 10, "");
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) return;
            this.hovered =
                    mouseX >= this.x
                            && mouseY >= this.y
                            && mouseX < this.x + this.width
                            && mouseY < this.y + this.height;
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(TEXTURES);
            int u = 131;
            int v = this.hovered ? 10 : 0;
            Gui.drawModalRectWithCustomSizedTexture(
                    this.x, this.y, u, v, this.width, this.height, IMAGE_WIDTH, IMAGE_HEIGHT);
        }
    }

    public static class HideUnchangedButton extends GuiButton {

        public HideUnchangedButton(int id, int x, int y) {
            super(id, x, y, 10, 10, "");
            this.visible = false;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) return;
            this.hovered =
                    mouseX >= this.x
                            && mouseY >= this.y
                            && mouseX < this.x + this.width
                            && mouseY < this.y + this.height;
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(TEXTURES);
            int u = 131;
            int v = 20;
            int vOffset = hideUnchanged ? 0 : 10;
            if (this.hovered) vOffset += 20;
            Gui.drawModalRectWithCustomSizedTexture(
                    this.x, this.y, u, v + vOffset, this.width, this.height, IMAGE_WIDTH, IMAGE_HEIGHT);
        }
    }
}
