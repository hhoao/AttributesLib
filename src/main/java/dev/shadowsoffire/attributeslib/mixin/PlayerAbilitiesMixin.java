package dev.shadowsoffire.attributeslib.mixin;

import dev.shadowsoffire.attributeslib.util.IEntityOwned;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerCapabilities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * For the Creative Flight Attribute, the Capabilities must be aware of the owning Player so that
 * the {@code allowFlying} / {@code isFlying} fields can be updated to reflect the attribute value.
 */
@Mixin(PlayerCapabilities.class)
public class PlayerAbilitiesMixin implements IEntityOwned {

    protected EntityLivingBase owner;

    @Override
    public EntityLivingBase getOwner() {
        return owner;
    }

    @Override
    public void setOwner(EntityLivingBase owner) {
        if (this.owner != null)
            throw new UnsupportedOperationException("Cannot set the owner when it is already set.");
        if (owner == null) throw new UnsupportedOperationException("Cannot set the owner to null.");
        this.owner = owner;
    }
}
