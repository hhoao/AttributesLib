package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IForgeItem.class)
public interface IForgeItemMixin {

    /**
     * This mixin overwrites {@link IForgeItem#canElytraFly(ItemStack, LivingEntity)} to read the value of {@link Attributes#ELYTRA_FLIGHT}.
     *
     * @author Shadows
     * @reason Enables usage of {@link Attributes#ELYTRA_FLIGHT}.
     * @implNote If the user is wearing a chestplate that overrides this method, the attribute will be ignored.
     */
    @Overwrite(remap = false)
    default boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return entity.getAttributeValue(Attributes.ELYTRA_FLIGHT.get()) > 0;
    }

    /**
     * This mixin overwrites {@link IForgeItem#elytraFlightTick(ItemStack, LivingEntity, int)} to read the value of {@link Attributes#ELYTRA_FLIGHT}.
     *
     * @author Shadows
     * @reason Enables usage of {@link Attributes#ELYTRA_FLIGHT}.
     * @implNote If the user is wearing a chestplate that overrides this method, the attribute will be ignored.
     */
    @Overwrite(remap = false)
    default boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return entity.getAttributeValue(Attributes.ELYTRA_FLIGHT.get()) > 0;
    }
}
