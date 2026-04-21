package dev.shadowsoffire.attributeslib.api.client;

import java.util.List;
import java.util.ListIterator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Fired to add extra attribute tooltip lines without having to manually locate the inject point.
 *
 * <p>Fired on {@link MinecraftForge#EVENT_BUS}. Client only ({@link Side#CLIENT}).
 */
public class AddAttributeTooltipsEvent extends PlayerEvent {

    protected final ItemStack stack;
    protected final List<ITextComponent> tooltip;
    protected final ListIterator<ITextComponent> attributeTooltipIterator;
    protected final ITooltipFlag flag;

    public AddAttributeTooltipsEvent(
            ItemStack stack,
            EntityPlayer player,
            List<ITextComponent> tooltip,
            ListIterator<ITextComponent> attributeTooltipIterator,
            ITooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.tooltip = tooltip;
        this.attributeTooltipIterator = attributeTooltipIterator;
        this.flag = flag;
    }

    public ITooltipFlag getFlags() {
        return this.flag;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public List<ITextComponent> getTooltip() {
        return this.tooltip;
    }

    public ListIterator<ITextComponent> getAttributeTooltipIterator() {
        return this.attributeTooltipIterator;
    }

    /** May return null when the event is fired during search-tree bootstrap. */
    @Override
    public EntityPlayer getEntityPlayer() {
        return super.getEntityPlayer();
    }
}
