package dev.shadowsoffire.attributeslib;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import dev.shadowsoffire.attributeslib.packet.CritParticleMessage;
import dev.shadowsoffire.placebo.config.DeferredHelper;
import dev.shadowsoffire.placebo.network.MessageHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AttributesLib.MODID)
public class AttributesLib {

    public static final String MODID = "attributeslib";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static DeferredHelper R;

    /**
     * Static record of {@link Player#getAttackStrengthScale(float)} for use in damage events.<br>
     * Recorded in the {@link AttackEntityEvent} and valid for the entire chain, when a player
     * attacks.
     */
    public static float localAtkStrength = 1;

    public static int knowledgeMult = 4;

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, MODID))
                    .clientAcceptedVersions(s -> true)
                    .serverAcceptedVersions(s -> true)
                    .networkProtocolVersion(() -> "1.0.0")
                    .simpleChannel();

    public AttributesLib() {
        R = DeferredHelper.create(MODID);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(new AttributeEvents());
        if (FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.register(new AttributesLibClient());
            FMLJavaModLoadingContext.get().getModEventBus().register(AttributesLibClient.class);
        }

        MessageHelper.registerMessage(CHANNEL, 0, new CritParticleMessage.Provider());
        ALObjects.bootstrap();
        ALConfig.load();
    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(ALObjects.MobEffects.KNOWLEDGE);
        e.enqueueWork(
                () -> {
                    Effects.BLINDNESS.addAttributesModifier(
                            Attributes.FOLLOW_RANGE,
                            "f8c3de3d-1fea-4d7c-a8b0-22f63c4c3454",
                            -0.75,
                            AttributeModifier.Operation.MULTIPLY_TOTAL);
                    // TODO: Update to show in GUI without applying attribute to entity
                    // if (MobEffects.SLOW_FALLING.getAttributeModifiers().isEmpty()) {
                    // MobEffects.SLOW_FALLING.addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(),
                    // "A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA", -0.07, Operation.ADDITION);
                    // }
                });
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {

        event.enqueueWork(
                () -> {
                    List<EntityType<?>> collect =
                            ForgeRegistries.ENTITIES.getValues().stream()
                                    .filter((GlobalEntityTypeAttributes::doesEntityHaveAttributes))
                                    .collect(Collectors.toList());
                    for (EntityType<?> value : collect) {
                        AttributeModifierMap attributesForEntity =
                                GlobalEntityTypeAttributes.getAttributesForEntity(
                                        (EntityType<? extends LivingEntity>) value);
                        Map<Attribute, ModifiableAttributeInstance> map =
                                new HashMap<>(attributesForEntity.attributeMap);
                        putAttribute(
                                map,
                                ALObjects.Attributes.DRAW_SPEED,
                                ALObjects.Attributes.CRIT_CHANCE,
                                ALObjects.Attributes.CRIT_DAMAGE,
                                ALObjects.Attributes.COLD_DAMAGE,
                                ALObjects.Attributes.FIRE_DAMAGE,
                                ALObjects.Attributes.LIFE_STEAL,
                                ALObjects.Attributes.CURRENT_HP_DAMAGE,
                                ALObjects.Attributes.OVERHEAL,
                                ALObjects.Attributes.GHOST_HEALTH,
                                ALObjects.Attributes.MINING_SPEED,
                                ALObjects.Attributes.ARROW_DAMAGE,
                                ALObjects.Attributes.ARROW_VELOCITY,
                                ALObjects.Attributes.EXPERIENCE_GAINED,
                                ALObjects.Attributes.HEALING_RECEIVED,
                                ALObjects.Attributes.ARMOR_PIERCE,
                                ALObjects.Attributes.ARMOR_SHRED,
                                ALObjects.Attributes.PROT_PIERCE,
                                ALObjects.Attributes.PROT_SHRED,
                                ALObjects.Attributes.DODGE_CHANCE,
                                ALObjects.Attributes.ELYTRA_FLIGHT,
                                ALObjects.Attributes.CREATIVE_FLIGHT);
                        AttributeModifierMap attributeModifierMap = new AttributeModifierMap(map);
                        GlobalEntityTypeAttributes.put(
                                (EntityType<? extends LivingEntity>) value, attributeModifierMap);
                    }
                    AttributeModifierMap playerAttribs =
                            GlobalEntityTypeAttributes.getAttributesForEntity(EntityType.PLAYER);
                    for (Attribute attr : ForgeRegistries.ATTRIBUTES.getValues()) {
                        if (playerAttribs.hasAttribute(attr)) attr.setShouldWatch(true);
                    }
                });
    }

    public void putAttribute(
            Map<Attribute, ModifiableAttributeInstance> map,
            RegistryObject<Attribute>... attributes) {
        for (RegistryObject<Attribute> attribute : attributes) {
            map.put(
                    attribute.get(),
                    new ModifiableAttributeInstance(attribute.get(), (attributeInstance -> {})));
        }
    }

    public static ITooltipFlag getTooltipFlag() {
        if (FMLEnvironment.dist.isClient()) return ClientAccess.getTooltipFlag();
        return ITooltipFlag.TooltipFlags.NORMAL;
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

    private static class ClientAccess {
        static ITooltipFlag getTooltipFlag() {
            return Minecraft.getInstance().gameSettings.advancedItemTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL;
        }
    }
}
