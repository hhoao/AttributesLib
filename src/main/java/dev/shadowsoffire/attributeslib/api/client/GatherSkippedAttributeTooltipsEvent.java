package dev.shadowsoffire.attributeslib.api.client;

import java.util.Set;
import java.util.UUID;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This event is used to collect UUIDs of attribute modifiers that will not be displayed in item
 * tooltips.
 *
 * <p>This allows hiding specific modifiers for whatever reason. They will still be shown in the
 * attributes GUI.
 *
 * <p>This event is fired on {@linkplain MinecraftForge#EVENT_BUS the main event bus}.<br>
 * This event is only fired on the {@linkplain Side#CLIENT physical client}.
 */
public class GatherSkippedAttributeTooltipsEvent extends PlayerEvent {

    protected final ItemStack stack;
    protected final Set<UUID> skips;
    protected final ITooltipFlag flag;

    public GatherSkippedAttributeTooltipsEvent(
            ItemStack stack, EntityPlayer player, Set<UUID> skips, ITooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.skips = skips;
        this.flag = flag;
    }

    /**
     * Use to determine if the advanced information on item tooltips is being shown, toggled by
     * F3+H.
     */
    public ITooltipFlag getFlags() {
        return this.flag;
    }

    /** The {@link ItemStack} with the tooltip. */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Mark the UUID of a specific attribute modifier as skipped, causing it to not be displayed in
     * the tooltip.
     */
    public void skipUUID(UUID id) {
        this.skips.add(id);
    }

    /**
     * This event is fired with a null player during startup when populating search trees for
     * tooltips.
     */
    @Override
    public EntityPlayer getEntityPlayer() {
        return super.getEntityPlayer();
    }
}
