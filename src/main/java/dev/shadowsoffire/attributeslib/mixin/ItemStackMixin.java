package dev.shadowsoffire.attributeslib.mixin;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    /**
     * Marks the point just before vanilla begins iterating equipment slots for attribute tooltip
     * output. The marker is always removed later during {@code ItemTooltipEvent}, even when the
     * item ends up contributing no modifier lines.
     */
    @Inject(
            method =
                    "getTooltip(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/inventory/EntityEquipmentSlot;values()[Lnet/minecraft/inventory/EntityEquipmentSlot;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    private void attributeslib$tooltipMarkerStart(
            @Nullable EntityPlayer player,
            ITooltipFlag flag,
            CallbackInfoReturnable<List<String>> cir,
            List<String> tooltip,
            String name,
            int hideFlags) {
        tooltip.add("APOTH_REMOVE_MARKER");
    }

    /**
     * Marks the point just after the vanilla attribute section has been appended and before
     * non-modifier follow-up blocks begin.
     */
    @Inject(
            method =
                    "getTooltip(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at =
                    @At(
                            value = "INVOKE",
                            ordinal = 3,
                            target = "Lnet/minecraft/item/ItemStack;hasTagCompound()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    private void attributeslib$tooltipMarkerEnd(
            @Nullable EntityPlayer player,
            ITooltipFlag flag,
            CallbackInfoReturnable<List<String>> cir,
            List<String> tooltip,
            String name,
            int hideFlags,
            net.minecraft.inventory.EntityEquipmentSlot[] slots,
            int slotCount,
            int slotIdx) {
        tooltip.add("APOTH_REMOVE_MARKER_2");
    }
}
