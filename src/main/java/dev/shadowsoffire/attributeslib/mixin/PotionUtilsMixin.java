package dev.shadowsoffire.attributeslib.mixin;

import com.mojang.datafixers.util.Pair;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {

    /**
     * Redirects the second {@link List#isEmpty()} call that is checked before adding tooltips to
     * potions to replace vanilla tooltip handling.<br>
     * Target Line: <code>if (!list.isEmpty()) {</code>.
     *
     * @param list The potion's attribute modifiers.
     * @param itemStack The potion stack.
     * @param tooltips The tooltip list.
     * @param durationFactor The duration factor of the potion.
     * @return True, unconditionally, so that the vanilla tooltip logic is ignored.
     * @see PotionUtils#addPotionTooltip(ItemStack, List, float)
     */
    @Redirect(
            method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1),
            require = 1)
    private static boolean attributeslib_potionTooltips(
            List<Pair<Attribute, AttributeModifier>> list,
            ItemStack itemStack,
            List<Component> tooltips,
            float durationFactor) {
        if (!list.isEmpty()) {
            tooltips.add(new TextComponent(""));
            tooltips.add(
                    new TranslatableComponent("potion.whenDrank")
                            .withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Attribute, AttributeModifier> pair : list) {
                tooltips.add(
                        IFormattableAttribute.toComponent(
                                pair.getFirst(), pair.getSecond(), AttributesLib.getTooltipFlag()));
            }
        }
        return true;
    }
}
