package dev.shadowsoffire.attributeslib;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import dev.shadowsoffire.attributeslib.packet.CritParticleMessage;
import dev.shadowsoffire.placebo.config.DeferredHelper;
import dev.shadowsoffire.placebo.network.MessageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = AttributesLib.MODID,
        name = AttributesLib.NAME,
        version = AttributesLib.VERSION,
        dependencies = "required-after:forge",
        acceptedMinecraftVersions = "[1.12,1.13)")
public class AttributesLib {

    public static final String MODID = "attributeslib";
    public static final String NAME = "Attributes Library";
    public static final String VERSION = "1.0.0";
    public static Logger LOGGER;
    public static DeferredHelper R;

    /**
     * Static record of {@code EntityPlayer#getCooledAttackStrength} for use in damage events.
     * Recorded in the {@link AttackEntityEvent} and valid for the entire chain when a player attacks.
     */
    public static float localAtkStrength = 1;

    public static int knowledgeMult = 4;

    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        LOGGER = e.getModLog();
        R = DeferredHelper.create(MODID);

        MinecraftForge.EVENT_BUS.register(new AttributeEvents());
        if (FMLCommonHandler.instance().getSide().isClient()) {
            AttributesLibClient.register();
        }

        MessageHelper.registerMessage(CHANNEL, 0, new CritParticleMessage.Provider());
        ALObjects.bootstrap();
        ALConfig.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(ALObjects.MobEffects.KNOWLEDGE.get());

        // Vanilla blindness attribute modifier — shrinks follow range. 1.12.2 registers through
        // Potion#registerPotionAttributeModifier, which takes (IAttribute, uuid, amount, op)
        // where op is 0=ADDITION, 1=MULTIPLY_BASE, 2=MULTIPLY_TOTAL.
        MobEffects.BLINDNESS.registerPotionAttributeModifier(
                SharedMonsterAttributes.FOLLOW_RANGE,
                "f8c3de3d-1fea-4d7c-a8b0-22f63c4c3454",
                -0.75D,
                2);
    }

    public static ITooltipFlag getTooltipFlag() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            return ClientAccess.getTooltipFlag();
        }
        return ITooltipFlag.TooltipFlags.NORMAL;
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

    @SideOnly(Side.CLIENT)
    private static class ClientAccess {
        static ITooltipFlag getTooltipFlag() {
            return Minecraft.getMinecraft().gameSettings.advancedItemTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL;
        }
    }
}
