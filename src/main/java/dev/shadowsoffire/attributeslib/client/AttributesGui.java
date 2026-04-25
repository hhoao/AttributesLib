package dev.shadowsoffire.attributeslib.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributesGui extends Widget {

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
    protected final PlayerEntity player;
    protected final ImageButton toggleBtn;
    protected final ImageButton recipeBookButton;
    protected final HideUnchangedButton hideUnchangedBtn;
    private final IRenderTypeBuffer.Impl bufferSource;
    private final FontRenderer font;
    private final ItemRenderer itemRenderer;

    protected int leftPos, topPos;
    protected boolean scrolling;
    protected int startIndex;
    protected List<ModifiableAttributeInstance> data = new ArrayList<>();
    @Nullable protected ModifiableAttributeInstance selected = null;
    protected boolean open = false;
    protected long lastRenderTick = -1;

    public AttributesGui(InventoryScreen parent) {
        super(
                parent.getGuiLeft() - WIDTH,
                parent.getGuiTop(),
                WIDTH,
                parent.height,
                StringTextComponent.EMPTY);
        //        super(parent.getTitle());
        this.parent = parent;
        this.bufferSource = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        this.font = Minecraft.getInstance().fontRenderer;
        this.player = Minecraft.getInstance().player;
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.refreshData();
        //        this.width = WIDTH;
        //        this.height = parent.height;
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
                        new StringTextComponent("attributeslib.gui.show_attributes")) {
                    @Override
                    public void setFocused(boolean pFocused) {}
                };
        if (this.parent.children.size() > 1) {
            IGuiEventListener btn = this.parent.children.get(1);
            if (btn instanceof ImageButton) {
                this.recipeBookButton = (ImageButton) btn;
            } else {
                this.recipeBookButton = null;
            }
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
        if (this.open
                && this.parent.getRecipeGui().field_201522_g != null
                && this.parent.getRecipeGui().isVisible()) {
            this.parent.getRecipeGui().toggleVisibility();
        }
        this.hideUnchangedBtn.visible = this.open;

        int newLeftPos;
        if (this.open && this.parent.width >= 379) {
            newLeftPos = 177 + (this.parent.width - this.parent.getXSize() - 200) / 2;
        } else {
            newLeftPos = (this.parent.width - this.parent.getYSize()) / 2;
        }

        this.parent.guiLeft = newLeftPos;
        this.leftPos = this.parent.getGuiLeft() - WIDTH;
        this.topPos = this.parent.getGuiTop();

        if (this.recipeBookButton != null) {
            this.recipeBookButton.setPosition(
                    this.parent.getGuiLeft() + 104, this.parent.height / 2 - 22);
        }
        this.hideUnchangedBtn.setPosition(this.leftPos + 7, this.topPos + 151);
    }

    protected int compareAttrs(ModifiableAttributeInstance a1, ModifiableAttributeInstance a2) {
        String name = I18n.format(a1.getAttribute().getAttributeName());
        String name2 = I18n.format(a2.getAttribute().getAttributeName());
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
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.toggleBtn.x = this.parent.getGuiLeft() + 63;
        this.toggleBtn.y = this.parent.getGuiTop() + 10;
        if (this.parent.getRecipeGui().isVisible()) this.open = false;
        wasOpen = this.open;
        if (!this.open) return;

        this.refreshData();

        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURES);
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
        font.func_243248_b(
                poseStack,
                new TranslationTextComponent("attributeslib.gui.attributes"),
                this.leftPos + 8,
                this.topPos + 5,
                0x404040);
        font.func_243248_b(
                poseStack,
                new StringTextComponent("Hide Unchanged"),
                this.leftPos + 20,
                this.topPos + 152,
                0x404040);
    }

    @SuppressWarnings("deprecation")
    protected void renderTooltip(MatrixStack poseStack, int mouseX, int mouseY) {
        ModifiableAttributeInstance inst = this.getHoveredSlot(mouseX, mouseY);
        if (inst != null) {

            boolean isDynamic =
                    Registry.ATTRIBUTE
                            .getKey(inst.getAttribute())
                            .equals(ALObjects.Tags.DYNAMIC_BASE_ATTTE);

            Attribute attr = inst.getAttribute();
            IFormattableAttribute fAttr = (IFormattableAttribute) attr;
            List<ITextComponent> list = new ArrayList<>();
            IFormattableTextComponent name =
                    new TranslationTextComponent(attr.getAttributeName())
                            .mergeStyle(
                                    Style.EMPTY
                                            .createStyleFromFormattings(TextFormatting.GOLD)
                                            .setUnderlined(true));

            if (isDynamic) {
                name.append(
                        new StringTextComponent((" (dynamic)"))
                                .mergeStyle(
                                        Style.EMPTY
                                                .applyFormatting(TextFormatting.DARK_GRAY)
                                                .setUnderlined(false)));
            }

            if (AttributesLib.getTooltipFlag().isAdvanced()) {

                Style style = Style.EMPTY.applyFormatting(TextFormatting.GRAY).setUnderlined(false);
                name.append(
                        new StringTextComponent(
                                        " [" + ForgeRegistries.ATTRIBUTES.getKey(attr) + "]")
                                .mergeStyle(style));
            }

            list.add(name);

            String key = attr.getAttributeName() + ".desc";

            if (I18n.hasKey(key)) {
                IFormattableTextComponent txt =
                        new TranslationTextComponent(key)
                                .mergeStyle(TextFormatting.YELLOW, TextFormatting.ITALIC);
                list.add(txt);
            } else if (AttributesLib.getTooltipFlag().isAdvanced()) {
                IFormattableTextComponent txt =
                        new StringTextComponent(key)
                                .mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC);
                list.add(txt);
            }

            TextFormatting color = TextFormatting.GRAY;
            if (attr instanceof RangedAttribute) {
                if (inst.getValue() > inst.getBaseValue()) {
                    color = TextFormatting.YELLOW;
                } else if (inst.getValue() < inst.getBaseValue()) {
                    color = TextFormatting.RED;
                }
            }

            ITextComponent valueComp =
                    fAttr.toValueComponent(null, inst.getValue(), AttributesLib.getTooltipFlag())
                            .mergeStyle(color);
            ITextComponent baseComp =
                    fAttr.toValueComponent(
                                    null, inst.getBaseValue(), AttributesLib.getTooltipFlag())
                            .mergeStyle(TextFormatting.GRAY);

            if (!isDynamic) {
                list.add(StringTextComponent.EMPTY);
                list.add(
                        new TranslationTextComponent("attributeslib.gui.current", valueComp)
                                .mergeStyle(TextFormatting.GRAY));

                IFormattableTextComponent base =
                        new TranslationTextComponent("attributeslib.gui.base", baseComp)
                                .mergeStyle(TextFormatting.GRAY);

                if (attr instanceof RangedAttribute) {
                    RangedAttribute ra = (RangedAttribute) attr;
                    ITextComponent min =
                            fAttr.toValueComponent(
                                    null, ra.minimumValue, AttributesLib.getTooltipFlag());
                    min = new TranslationTextComponent("attributeslib.gui.min", min);
                    ITextComponent max =
                            fAttr.toValueComponent(
                                    null, ra.maximumValue, AttributesLib.getTooltipFlag());
                    max = new TranslationTextComponent("attributeslib.gui.max", max);
                    list.add(
                            new TranslationTextComponent("%s ┇ %s ┇ %s", base, min, max)
                                    .mergeStyle(TextFormatting.GRAY));
                } else {
                    list.add(base);
                }
            }

            List<ITextProperties> finalTooltip = new ArrayList<>(list.size());
            for (ITextComponent txt : list) {
                this.addComp(txt, finalTooltip);
            }

            if (inst.getModifierListCopy().stream().anyMatch(modif -> modif.getAmount() != 0)) {
                this.addComp(StringTextComponent.EMPTY, finalTooltip);
                this.addComp(
                        new TranslationTextComponent("attributeslib.gui.modifiers")
                                .mergeStyle(TextFormatting.GOLD),
                        finalTooltip);

                Map<UUID, ModifierSource<?>> modifiersToSources = new HashMap<>();

                for (ModifierSourceType type : ModifierSourceType.getTypes()) {
                    type.extract(
                            this.player,
                            (modif, source) -> modifiersToSources.put(modif.getID(), source));
                }

                ITextComponent[] opValues = new ITextComponent[3];
                double[] numericValues = new double[3];

                for (AttributeModifier.Operation op : AttributeModifier.Operation.values()) {
                    List<AttributeModifier> modifiers = new ArrayList<>(inst.getModifierListCopy());
                    double opValue =
                            modifiers.stream()
                                    .mapToDouble(AttributeModifier::getAmount)
                                    .reduce(
                                            op == AttributeModifier.Operation.MULTIPLY_TOTAL
                                                    ? 1
                                                    : 0,
                                            (res, elem) ->
                                                    op == AttributeModifier.Operation.MULTIPLY_TOTAL
                                                            ? res * (1 + elem)
                                                            : res + elem);

                    modifiers.sort(ModifierSourceType.compareBySource(modifiersToSources));
                    for (AttributeModifier modif : modifiers) {
                        if (modif.getAmount() != 0) {
                            IFormattableTextComponent comp =
                                    fAttr.toComponent(modif, AttributesLib.getTooltipFlag());
                            ModifierSource src = modifiersToSources.get(modif.getID());
                            finalTooltip.add(
                                    new AttributeModifierComponent(
                                            src, comp, this.font, this.leftPos - 16));
                        }
                    }
                    color = TextFormatting.GRAY;
                    double threshold =
                            op == AttributeModifier.Operation.MULTIPLY_TOTAL ? 1.0005 : 0.0005;

                    if (opValue > threshold) {
                        color = TextFormatting.YELLOW;
                    } else if (opValue < -threshold) {
                        color = TextFormatting.RED;
                    }
                    ITextComponent valueComp2 =
                            fAttr.toValueComponent(op, opValue, AttributesLib.getTooltipFlag())
                                    .deepCopy()
                                    .mergeStyle(color);
                    ITextComponent comp =
                            new TranslationTextComponent(
                                            "attributeslib.gui."
                                                    + op.name().toLowerCase(Locale.ROOT),
                                            valueComp2)
                                    .mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC);
                    opValues[op.ordinal()] = comp;
                    numericValues[op.ordinal()] = opValue;
                }

                this.addComp(StringTextComponent.EMPTY, finalTooltip);
                this.addComp(
                        new StringTextComponent("Modifier Formula").mergeStyle(TextFormatting.GOLD),
                        finalTooltip);

                ITextComponent base =
                        isDynamic
                                ? new TranslationTextComponent("attributeslib.gui.formula.base")
                                : baseComp;
                ITextComponent value =
                        isDynamic
                                ? new TranslationTextComponent("attributeslib.gui.formula.value")
                                : valueComp;

                ITextComponent formula = buildFormula(base, value, numericValues);
                this.addComp(formula, finalTooltip);
            } else if (isDynamic) {
                this.addComp(StringTextComponent.EMPTY, finalTooltip);
                this.addComp(
                        new TranslationTextComponent("attributeslib.gui.no_modifiers")
                                .mergeStyle(TextFormatting.GOLD),
                        finalTooltip);
            }

            parent.renderWrappedToolTip(
                    poseStack,
                    finalTooltip,
                    this.leftPos
                            - 16
                            - finalTooltip.stream()
                                    .map(c -> font.getStringPropertyWidth(c))
                                    .max(Integer::compare)
                                    .get(),
                    mouseY,
                    font);
        }
    }

    private void addComp(ITextComponent comp, List<ITextProperties> finalTooltip) {
        if (Objects.equals(comp, StringTextComponent.EMPTY)) {
            finalTooltip.add(comp);
        } else {
            finalTooltip.addAll(
                    this.font
                            .getCharacterManager()
                            .func_238362_b_(comp, this.leftPos - 16, comp.getStyle()));
        }
    }

    @SuppressWarnings("deprecation")
    private void renderEntry(
            MatrixStack poseStack,
            ModifiableAttributeInstance inst,
            int x,
            int y,
            int mouseX,
            int mouseY) {
        boolean hover = this.getHoveredSlot(mouseX, mouseY) == inst;
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURES);
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

        ITextComponent txt = new TranslationTextComponent(inst.getAttribute().getAttributeName());
        int splitWidth = 60;
        List<IReorderingProcessor> lines = this.font.trimStringToWidth(txt, splitWidth);
        // We can only actually display two lines here, but we need to forcibly create two lines and
        // then scale down.
        while (lines.size() > 2) {
            splitWidth += 10;
            lines = this.font.trimStringToWidth(txt, splitWidth);
        }

        poseStack.push();
        float scale = 1;
        int maxWidth = lines.stream().map(this.font::func_243245_a).max(Integer::compareTo).get();
        if (maxWidth > 66) {
            scale = 66F / maxWidth;
            poseStack.scale(scale, scale, 1);
        }

        for (int i = 0; i < lines.size(); i++) {
            IReorderingProcessor line = lines.get(i);
            float width = this.font.func_243245_a(line) * scale;
            float lineX = (x + 1 + (68 - width) / 2) / scale;
            float lineY = (y + (lines.size() == 1 ? 7 : 2) + i * 10) / scale;
            Minecraft.getInstance().getTextureManager().bindTexture(TEXTURES);
            font.func_238422_b_(poseStack, line, lineX, lineY, 0x404040);
        }
        poseStack.push();
        poseStack.pop();

        IFormattableAttribute attr = (IFormattableAttribute) inst.getAttribute();
        ITextComponent value =
                attr.toValueComponent(null, inst.getValue(), ITooltipFlag.TooltipFlags.NORMAL);

        if (Registry.ATTRIBUTE
                .getKey(inst.getAttribute())
                .equals(ALObjects.Tags.DYNAMIC_BASE_ATTTE)) {
            value = new StringTextComponent("\uFFFD");
        }

        scale = 1;
        if (this.font.getStringPropertyWidth(value) > 27) {
            scale = 27F / this.font.getStringPropertyWidth(value);
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
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURES);
        // drawShadow
        font.func_243248_b(
                poseStack,
                value,
                (int)
                        ((x + 72 + (27 - this.font.getStringPropertyWidth(value) * scale) / 2)
                                / scale),
                (int) ((y + 7) / scale),
                color);

        poseStack.pop();
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
            scrollOffset = MathHelper.clamp(scrollOffset, 0.0F, 1.0F);
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
            scrollOffset = MathHelper.clamp(scrollOffset, 0.0F, 1.0F);
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
            scrollOffset = MathHelper.clamp(scrollOffset, 0.0F, 1.0F);
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

    @Nullable
    public ModifiableAttributeInstance getHoveredSlot(int mouseX, int mouseY) {
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

    private static DecimalFormat f = ItemStack.DECIMALFORMAT;

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
    public static IFormattableTextComponent buildFormula(
            ITextComponent base, ITextComponent value, double[] numericValues) {
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
            TextFormatting color = isAddNeg ? TextFormatting.RED : TextFormatting.YELLOW;
            formula = formula + " " + colored(addSym + " " + addStr, color);
        }

        if (mulBase != 0) {
            String withParens = add == 0 ? formula : String.format("(%s)", formula);
            TextFormatting color = isMulNeg ? TextFormatting.RED : TextFormatting.YELLOW;
            formula =
                    withParens
                            + " "
                            + colored(mulBaseSym + " " + mulBaseStr + " * ", color)
                            + withParens;
        }

        if (mulTotal != 1) {
            String withParens = add == 0 && mulBase == 0 ? formula : String.format("(%s)", formula);
            TextFormatting color = mulTotal < 1 ? TextFormatting.RED : TextFormatting.YELLOW;
            formula = colored(mulTotalStr + " * ", color) + withParens;
        }

        return new TranslationTextComponent("%1$s = " + formula, value, base)
                .mergeStyle(TextFormatting.GRAY);
    }

    /**
     * Colors a string using legacy formatting codes. Terminates the string with {@link
     * TextFormatting#RESET}.
     */
    private static String colored(String str, TextFormatting color) {
        return "" + '§' + color.formattingCode + str + '§' + TextFormatting.RESET.formattingCode;
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
                    new StringTextComponent("Hide Unchanged Attributes"));
            this.visible = false;
        }

        @Override
        public void onPress() {
            hideUnchanged = !hideUnchanged;
        }

        @Override
        public void renderButton(MatrixStack stack, int pMouseX, int pMouseY, float pPartialTick) {
            int u = 131, v = 20;
            int vOffset = hideUnchanged ? 0 : 10;
            if (this.isHovered) {
                vOffset += 20;
            }

            RenderSystem.enableDepthTest();
            stack.push();
            stack.translate(0, 0, 100);
            blit(stack, this.x, this.y, u, v + vOffset, 10, 10, IMAGE_WIDTH, IMAGE_HEIGHT);
            stack.pop();
        }
    }
}
