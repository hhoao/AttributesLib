package dev.shadowsoffire.attributeslib.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired whenever the value of an attribute changes values.<br>
 * It is fired on both sides at different points:
 *
 * <ul>
 *   <li>On the Server, it is fired from an {@code AbstractAttributeMap} mixin on {@code
 *       onAttributeModified}, which is the builtin callback hook for values changing.
 *   <li>On the Client, it is fired from a {@code NetHandlerPlayClient} mixin after all changes
 *       from {@code SPacketEntityProperties} have been processed.
 * </ul>
 *
 * It is fired on {@link MinecraftForge#EVENT_BUS}.
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

    /** @return The Entity whose attribute was modified. */
    public EntityLivingBase getEntity() {
        return this.entity;
    }

    /** @return The Attribute instance whose value has changed. */
    public IAttributeInstance getAttributeInstance() {
        return this.attrInst;
    }

    /** @return The old value of the attribute, before the change occurred. */
    public double getOldValue() {
        return this.oldValue;
    }

    /** @return The new value of the attribute, after the change occurred. */
    public double getNewValue() {
        return this.newValue;
    }
}
