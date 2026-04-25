package dev.shadowsoffire.attributeslib.mixin;

import com.mojang.datafixers.util.Pair;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionContents.class)
public class PotionUtilsMixin {

    /**
     * Redirects the {@link List#isEmpty()} call that is checked before adding attribute modifier
     * tooltips to potions to replace vanilla tooltip handling.<br>
     * Target Line: <code>if (!list.isEmpty()) {</code>.
     *
     * @param list The potion's attribute modifiers.
     * @param tooltipAdder The tooltip consumer.
     * @param durationFactor The duration factor of the potion.
     * @return True, unconditionally, so that the vanilla tooltip logic is ignored.
     * @see PotionContents#addPotionTooltip(Iterable, Consumer, float, float)
     */
    @Redirect(
            method = "addPotionTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"),
            require = 1)
    private static boolean attributeslib_potionTooltips(
            List<Pair<Holder<Attribute>, AttributeModifier>> list,
            Iterable<MobEffectInstance> effects,
            Consumer<Component> tooltipAdder,
            float durationFactor,
            float ticksPerSecond) {
        if (!list.isEmpty()) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(
                    Component.translatable("potion.whenDrank")
                            .withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
                tooltipAdder.accept(
                        IFormattableAttribute.toComponent(
                                pair.getFirst().value(),
                                pair.getSecond(),
                                AttributesLib.getTooltipFlag()));
            }
        }
        return true;
    }
}
