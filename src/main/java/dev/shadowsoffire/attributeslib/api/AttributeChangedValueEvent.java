package dev.shadowsoffire.attributeslib.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when the value of an attribute changes.
 *
 * <p>Server: fired from an {@code AbstractAttributeMap} mixin on {@code onAttributeModified}.<br>
 * Client: fired from a {@code NetHandlerPlayClient} mixin after processing {@code
 * SPacketEntityProperties}.
 *
 * <p>Fires on {@link MinecraftForge#EVENT_BUS}.
 */
public class AttributeChangedValueEvent extends Event {

    protected final EntityLivingBase entity;
    protected final IAttributeInstance attrInst;
    protected final double oldValue;
    protected final double newValue;

    public AttributeChangedValueEvent(
            EntityLivingBase entity,
            IAttributeInstance attrInst,
            double oldValue,
            double newValue) {
        this.entity = entity;
        this.attrInst = attrInst;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public EntityLivingBase getEntity() {
        return this.entity;
    }

    public IAttributeInstance getAttributeInstance() {
        return this.attrInst;
    }

    public double getOldValue() {
        return this.oldValue;
    }

    public double getNewValue() {
        return this.newValue;
    }
}
