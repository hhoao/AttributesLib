package dev.shadowsoffire.attributeslib.api.client;

import java.util.List;
import java.util.ListIterator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This event is used to add additional attribute tooltip lines without having to manually locate
 * the inject point.
 *
 * <p>1.12.2 tooltips are plain {@link String} lines (including formatting codes), so listeners
 * insert formatted strings rather than {@code ITextComponent}s.
 *
 * <p>This event is fired on {@linkplain MinecraftForge#EVENT_BUS the main event bus}.<br>
 * This event is only fired on the {@linkplain Side#CLIENT physical client}.
 */
public class AddAttributeTooltipsEvent extends PlayerEvent {

    protected final ItemStack stack;
    protected final List<String> tooltip;
    protected final ListIterator<String> attributeTooltipIterator;
    protected final ITooltipFlag flag;

    public AddAttributeTooltipsEvent(
            ItemStack stack,
            EntityPlayer player,
            List<String> tooltip,
            ListIterator<String> attributeTooltipIterator,
            ITooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.tooltip = tooltip;
        this.attributeTooltipIterator = attributeTooltipIterator;
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

    /** The {@link ItemStack}'s full tooltip. */
    public List<String> getTooltip() {
        return this.tooltip;
    }

    /** Returns an iterator pointed at the tail of the attribute tooltips. */
    public ListIterator<String> getAttributeTooltipIterator() {
        return this.attributeTooltipIterator;
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
