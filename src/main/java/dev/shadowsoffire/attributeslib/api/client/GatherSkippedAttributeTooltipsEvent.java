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
 * Collects UUIDs of attribute modifiers that will not be displayed in item tooltips. Hidden
 * modifiers are still shown in the attributes GUI.
 *
 * <p>Fired on {@link MinecraftForge#EVENT_BUS}. Client only ({@link Side#CLIENT}).
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

    public ITooltipFlag getFlags() {
        return this.flag;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void skipUUID(UUID id) {
        this.skips.add(id);
    }

    /** May return null when the event is fired during search-tree bootstrap. */
    @Override
    public EntityPlayer getEntityPlayer() {
        return super.getEntityPlayer();
    }
}
