package dev.shadowsoffire.attributeslib.client;

import static net.minecraft.client.gui.GuiComponent.blit;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.shadowsoffire.attributeslib.util.Comparators;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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
     * @param itemRenderer
     * @param font
     * @param x
     * @param y
     */
    public abstract void render(
            PoseStack poseStack, ItemRenderer itemRenderer, Font font, int x, int y);

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
                    Comparator.comparing(LivingEntity::getEquipmentSlotForItem)
                            .reversed()
                            .thenComparing(
                                    Comparator.comparing(
                                            ItemStack::getItem,
                                            Comparators.idComparator(Registry.ITEM))),
                    data);
        }

        @Override
        public void render(
                PoseStack poseStack, ItemRenderer itemRenderer, Font font, int x, int y) {
            poseStack.pushPose();
            float scale = 0.5F;
            poseStack.scale(scale, scale, 1);
            poseStack.translate(1 + x / scale, 1 + y / scale, 0);
            itemRenderer.renderGuiItem(this.data, 0, 0);
            poseStack.popPose();
        }
    }

    public static class EffectModifierSource extends ModifierSource<MobEffectInstance> {

        @SuppressWarnings("deprecation")
        public EffectModifierSource(MobEffectInstance data) {
            super(
                    ModifierSourceType.MOB_EFFECT,
                    Comparator.comparing(
                            MobEffectInstance::getEffect,
                            Comparators.idComparator(Registry.MOB_EFFECT)),
                    data);
        }

        @Override
        public void render(
                PoseStack poseStack, ItemRenderer itemRenderer, Font font, int x, int y) {
            MobEffectTextureManager texMgr = Minecraft.getInstance().getMobEffectTextures();
            // We don't have an EffectRenderingInventoryScreen, so we'll just hope the texture is
            // good enough.
            // var renderer =
            // net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(inst);
            // if (renderer.renderInventoryIcon(inst, this, pPoseStack, pRenderX + (p_194013_ ? 6 :
            // 7), i, this.getBlitOffset())) {
            // i += pYOffset;
            // continue;
            // }
            MobEffect effect = this.data.getEffect();
            TextureAtlasSprite sprite = texMgr.get(effect);
            float scale = 0.5F;
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1);
            poseStack.translate(x / scale, y / scale, 0);
            blit(poseStack, 0, 0, 0, 18, 18, sprite);
            poseStack.popPose();
        }
    }
}
