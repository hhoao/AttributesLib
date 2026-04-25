package dev.shadowsoffire.attributeslib.client;

import static net.minecraft.client.gui.AbstractGui.blit;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.shadowsoffire.attributeslib.util.Comparators;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.registry.Registry;

/**
 * A Modifier Source is a container object around any potential Attribute Modifier Source.<br>
 * It has the code necessary to render and compare the object for display in the Attributes screen.
 */
public abstract class ModifierSource<T> implements Comparable<ModifierSource<T>> {

    protected final ModifierSourceType type;
    protected final Comparator<T> comparator;
    protected final T data;

    public ModifierSource(ModifierSourceType type, Comparator<T> comparator, T data) {
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
            MatrixStack poseStack, ItemRenderer itemRenderer, FontRenderer font, int x, int y);

    public ModifierSourceType getType() {
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
                    Comparator.comparing(MobEntity::getSlotForItemStack)
                            .reversed()
                            .thenComparing(
                                    Comparator.comparing(
                                            ItemStack::getItem,
                                            Comparators.idComparator(Registry.ITEM))),
                    data);
        }

        @Override
        public void render(
                MatrixStack poseStack, ItemRenderer itemRenderer, FontRenderer font, int x, int y) {
            poseStack.push();
            float scale = 0.5F;
            poseStack.scale(scale, scale, 1);
            poseStack.translate(1 + x / scale, 1 + y / scale, 0);
            itemRenderer.renderItemIntoGUI(this.data, 0, 0);
            poseStack.pop();
        }
    }

    public static class EffectModifierSource extends ModifierSource<EffectInstance> {

        @SuppressWarnings("deprecation")
        public EffectModifierSource(EffectInstance data) {
            super(
                    ModifierSourceType.MOB_EFFECT,
                    Comparator.comparing(
                            EffectInstance::getPotion, Comparators.idComparator(Registry.EFFECTS)),
                    data);
        }

        @Override
        public void render(
                MatrixStack poseStack, ItemRenderer itemRenderer, FontRenderer font, int x, int y) {
            PotionSpriteUploader texMgr = Minecraft.getInstance().getPotionSpriteUploader();

            // We don't have an EffectRenderingInventoryScreen, so we'll just hope the texture is
            // good enough.
            // var renderer =
            // net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(inst);
            // if (renderer.renderInventoryIcon(inst, this, pPoseStack, pRenderX + (p_194013_ ? 6 :
            // 7), i, this.getBlitOffset())) {
            // i += pYOffset;
            // continue;
            // }
            Effect effect = this.data.getPotion();
            TextureAtlasSprite sprite = texMgr.getSprite(effect);
            float scale = 0.5F;
            poseStack.push();
            poseStack.scale(scale, scale, 1);
            poseStack.translate(x / scale, y / scale, 0);
            blit(poseStack, 0, 0, 0, 18, 18, sprite);
            poseStack.pop();
        }
    }
}
