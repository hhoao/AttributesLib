package dev.shadowsoffire.attributeslib.client;

import static net.minecraft.client.gui.GuiComponent.blit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.shadowsoffire.attributeslib.AttributesLib;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class AttributeModifierComponent implements ClientTooltipComponent {

    public static final ResourceLocation TEXTURE =
            AttributesLib.loc("textures/gui/attribute_component.png");

    @Nullable private final ModifierSource<?> source;
    private final List<FormattedCharSequence> text;

    public AttributeModifierComponent(
            @Nullable ModifierSource<?> source, FormattedText text, Font font, int maxWidth) {
        this.source = source;
        this.text = font.split(text, maxWidth);
    }

    @Override
    public int getHeight() {
        return this.text.size() * 10;
    }

    @Override
    public int getWidth(Font font) {
        return this.text.stream().map(font::width).map(w -> w + 12).max(Integer::compareTo).get();
    }

    //    @Override
    //    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
    //        gfx.blit(TEXTURE, x, y, 0, this.source == null ? 9 : 0, 0, 9, 9, 18, 9);
    //        if (this.source == null) return;
    //        this.source.render(gfx, font, x, y);
    //    }

    @Override
    public void renderImage(
            Font font,
            int x,
            int y,
            PoseStack poseStack,
            ItemRenderer itemRenderer,
            int p_194053_) {
        // 设置当前的纹理
        RenderSystem.setShaderTexture(0, TEXTURE); // 设置纹理

        // 开启混合模式（使纹理透明）
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 使用 PoseStack 来应用矩阵变换
        poseStack.pushPose(); // 开始一个新的矩阵堆栈

        // 绘制纹理

        blit(poseStack, x, y, 0, this.source == null ? 9 : 0, 0, 9, 9, 18, 9);

        // 如果 source 不为空，继续渲染
        if (this.source != null) {
            this.source.render(poseStack, itemRenderer, font, x, y);
        }

        // 恢复矩阵堆栈
        poseStack.popPose();
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix4f, BufferSource bufferSource) {
        // 调用父类的默认实现，渲染文本
        ClientTooltipComponent.super.renderText(font, x, y, matrix4f, bufferSource);

        FormattedCharSequence line = this.text.get(0);

        // 使用 font.drawInBatch 进行渲染
        font.drawInBatch(
                line, // 要绘制的文本行
                x + 12, // X 坐标
                y, // Y 坐标
                -1, // 颜色，-1 表示默认颜色
                true, // 是否启用抗锯齿
                matrix4f, // 渲染矩阵（通常用于 3D）
                bufferSource, // 渲染缓冲区
                true,
                0, // 字体大小
                15728880); // 文本颜色（十六进制）

        // 渲染其余文本行
        for (int i = 1; i < this.text.size(); i++) {
            line = this.text.get(i);
            font.drawInBatch(
                    line,
                    x,
                    y + i * (font.lineHeight + 1),
                    -1,
                    true,
                    matrix4f,
                    bufferSource,
                    true,
                    0,
                    15728880);
        }
    }
}
