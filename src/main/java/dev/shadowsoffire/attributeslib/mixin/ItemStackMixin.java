package dev.shadowsoffire.attributeslib.mixin;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    // Injects just before ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember
    // where to rewind to.
    @Inject(
            method =
                    "addDetailsToTooltip(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;Ljava/util/function/Consumer;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/neoforged/neoforge/common/util/AttributeUtil;addAttributeTooltips(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V"),
            require = 1)
    public void apoth_tooltipMarker(
            Item.TooltipContext pContext,
            TooltipDisplay tooltipDisplay,
            @Nullable Player pPlayer,
            TooltipFlag pIsAdvanced,
            Consumer<Component> tooltipAdder,
            CallbackInfo ci) {
        tooltipAdder.accept(Component.literal("APOTH_REMOVE_MARKER"));
    }

    // Injects just after ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember
    // where to rewind to.
    @Inject(
            method =
                    "addDetailsToTooltip(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;Ljava/util/function/Consumer;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/neoforged/neoforge/common/util/AttributeUtil;addAttributeTooltips(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V",
                            shift = At.Shift.AFTER),
            require = 1)
    public void apoth_tooltipMarker2(
            Item.TooltipContext pContext,
            TooltipDisplay tooltipDisplay,
            @Nullable Player pPlayer,
            TooltipFlag pIsAdvanced,
            Consumer<Component> tooltipAdder,
            CallbackInfo ci) {
        tooltipAdder.accept(Component.literal("APOTH_REMOVE_MARKER_2"));
    }
}
