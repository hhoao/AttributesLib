package dev.shadowsoffire.attributeslib.mixin;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    // Injects just before ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember
    // where to rewind to.
    @Inject(
            method =
                    "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at =
                    @At(
                            value = "INVOKE",
                            ordinal = 3,
                            target =
                                    "Lnet/minecraft/item/ItemStack;func_242394_a(ILnet/minecraft/item/ItemStack$TooltipDisplayFlags;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    public void apoth_tooltipMarker(
            @Nullable PlayerEntity pPlayer,
            ITooltipFlag pIsAdvanced,
            CallbackInfoReturnable<List<ITextComponent>> cir,
            List<ITextComponent> list) {
        list.add(new StringTextComponent("APOTH_REMOVE_MARKER"));
    }

    // Injects just after ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember
    // where to rewind to.
    @Inject(
            method =
                    "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at =
                    @At(
                            value = "INVOKE",
                            ordinal = 1,
                            target = "Lnet/minecraft/item/ItemStack;hasTag()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    public void apoth_tooltipMarker2(
            @Nullable PlayerEntity pPlayer,
            ITooltipFlag pIsAdvanced,
            CallbackInfoReturnable<List<ITextComponent>> cir,
            List<ITextComponent> list) {
        list.add(new StringTextComponent("APOTH_REMOVE_MARKER_2"));
    }
}
