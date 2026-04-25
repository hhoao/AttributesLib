package dev.shadowsoffire.attributeslib.util;

/** A manager for handling weird attribute logic within Minecraft. */
public interface IAttributeManager {

    /** {@return whether the attributes are being updated, instead of added or removed} */
    boolean areAttributesUpdating();

    /**
     * Sets whether the attributes are being updated, instead of added or removed.
     *
     * @param updating whether the attributes are being updated, instead of added or removed
     */
    void setAttributesUpdating(boolean updating);
}
