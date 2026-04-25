package dev.shadowsoffire.attributeslib.mixin;

import net.minecraft.potion.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;

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
    //    @Redirect(
    //            method = "addPotionTooltip(Lnet/minecraft/item/ItemStack;Ljava/util/List;F)V",
    //            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1),
    //            require = 1)
    //    private static boolean attributeslib_potionTooltips(
    //            List<Pair<Attribute, AttributeModifier>> list,
    //            ItemStack itemStack,
    //            List<ITextComponent> tooltips,
    //            float durationFactor) {
    //        if (!list.isEmpty()) {
    //            tooltips.add(new StringTextComponent(""));
    //            tooltips.add(
    //                    new TranslationTextComponent("potion.whenDrank")
    //                        .mergeStyle(TextFormatting.DARK_PURPLE));
    //
    //            for (Pair<Attribute, AttributeModifier> pair : list) {
    //                tooltips.add(
    //                        IFormattableAttribute.toComponent(
    //                                pair.getFirst(), pair.getSecond(),
    // AttributesLib.getTooltipFlag()));
    //            }
    //        }
    //        return true;
    //    }
}
