package dev.shadowsoffire.attributeslib.client;

import dev.shadowsoffire.attributeslib.util.Comparators;
import java.util.Comparator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * A Modifier Source is a container object around any potential Attribute Modifier Source.<br>
 * It has the code necessary to render and compare the object for display in the Attributes screen.
 */
public abstract class ModifierSource<T> implements Comparable<ModifierSource<T>> {

    protected final ModifierSourceType<T> type;
    protected final Comparator<T> comparator;
    protected final T data;

    public ModifierSource(ModifierSourceType<T> type, Comparator<T> comparator, T data) {
        this.type = type;
        this.comparator = comparator;
        this.data = data;
    }

    /**
     * Render this ModifierSource as whatever visual representation it may take.
     *
     * @param gfx
     * @param font
     * @param x
     * @param y
     */
    public abstract void render(GuiGraphics gfx, Font font, int x, int y);

    public ModifierSourceType<T> getType() {
        return this.type;
    }

    public final T getData() {
        return this.data;
    }

    @Override
    public int compareTo(ModifierSource<T> o) {
        return this.comparator.compare(this.getData(), o.getData());
    }

    public static class ItemModifierSource extends ModifierSource<ItemStack> {

        @SuppressWarnings("deprecation")
        public ItemModifierSource(ItemStack data) {
            super(
                    ModifierSourceType.EQUIPMENT,
                    Comparator.comparing(
                            ItemStack::getItem, Comparators.idComparator(BuiltInRegistries.ITEM)),
                    data);
        }

        @Override
        public void render(GuiGraphics gfx, Font font, int x, int y) {
            Matrix3x2fStack pose = gfx.pose();
            pose.pushMatrix();
            float scale = 0.5F;
            pose.scale(scale, scale);
            pose.translate(1 + x / scale, 1 + y / scale);
            gfx.renderFakeItem(this.data, 0, 0);
            pose.popMatrix();
        }
    }

    public static class EffectModifierSource extends ModifierSource<MobEffectInstance> {

        @SuppressWarnings("deprecation")
        public EffectModifierSource(MobEffectInstance data) {
            super(
                    ModifierSourceType.MOB_EFFECT,
                    Comparator.comparing(
                            inst -> inst.getEffect().value(),
                            Comparators.idComparator(BuiltInRegistries.MOB_EFFECT)),
                    data);
        }

        @Override
        public void render(GuiGraphics gfx, Font font, int x, int y) {
            // We don't have an EffectRenderingInventoryScreen, so we'll just hope the texture is
            // good enough.
            // var renderer =
            // net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions.of(inst);
            // if (renderer.renderInventoryIcon(inst, this, pPoseStack, pRenderX + (p_194013_ ? 6 :
            // 7), i, this.getBlitOffset())) {
            // i += pYOffset;
            // continue;
            // }
            float scale = 0.5F;
            Matrix3x2fStack stack = gfx.pose();
            stack.pushMatrix();
            stack.scale(scale, scale);
            stack.translate(x / scale, y / scale);
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(this.data.getEffect()), 0, 0, 18, 18);
            stack.popMatrix();
        }
    }
}
