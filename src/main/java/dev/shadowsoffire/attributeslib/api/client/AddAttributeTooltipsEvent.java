package dev.shadowsoffire.attributeslib.api.client;

import java.util.List;
import java.util.ListIterator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * This event is used to add additional attribute tooltip lines without having to manually locate
 * the inject point.
 *
 * <p>This event is fired on {@linkplain MinecraftForge#EVENT_BUS the main event bus}.<br>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class AddAttributeTooltipsEvent extends PlayerEvent {

    protected final ItemStack stack;
    protected final List<ITextComponent> tooltip;
    protected final ListIterator<ITextComponent> attributeTooltipIterator;
    protected final ITooltipFlag flag;

    public AddAttributeTooltipsEvent(
            ItemStack stack,
            PlayerEntity player,
            List<ITextComponent> tooltip,
            ListIterator<ITextComponent> attributeTooltipIterator,
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
    public List<ITextComponent> getTooltip() {
        return this.tooltip;
    }

    /** Returns an iterator pointed at the tail of the attribute tooltips. */
    public ListIterator<ITextComponent> getAttributeTooltipIterator() {
        return this.attributeTooltipIterator;
    }

    /**
     * This event is fired with a null player during startup when populating search trees for
     * tooltips.
     */
    @Override
    public PlayerEntity getEntity() {
        return super.getPlayer();
    }
}
