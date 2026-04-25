package dev.shadowsoffire.attributeslib;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import dev.shadowsoffire.attributeslib.packet.CritParticleMessage;
import dev.shadowsoffire.placebo.config.DeferredHelper;
import dev.shadowsoffire.placebo.config.RegistryObject;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AttributesLib.MODID)
public class AttributesLib {

    public static final String MODID = "attributeslib";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final DeferredHelper R = DeferredHelper.create(MODID);

    /**
     * Static record of {@link Player#getAttackStrengthScale(float)} for use in damage events.<br>
     * Recorded in the {@link net.neoforged.neoforge.event.entity.player.AttackEntityEvent} and
     * valid for the entire chain, when a player attacks.
     */
    public static float localAtkStrength = 1;

    public static int knowledgeMult = 4;

    public AttributesLib(IEventBus modEventBus) {
        ALObjects.bootstrap();
        R.register(modEventBus);
        modEventBus.register(this);
        NeoForge.EVENT_BUS.register(new AttributeEvents());
        if (FMLEnvironment.dist.isClient()) {
            AttributesLibClient clientEvents = new AttributesLibClient();
            NeoForge.EVENT_BUS.register(clientEvents);
            modEventBus.addListener(clientEvents::clientReload);
            modEventBus.addListener(clientEvents::particleFactories);
        }

        ALConfig.load();
    }

    @SubscribeEvent
    public void registerPayloads(RegisterPayloadHandlersEvent e) {
        e.registrar(MODID)
                .versioned("1.0.0")
                .playToClient(
                        CritParticleMessage.TYPE,
                        CritParticleMessage.STREAM_CODEC,
                        CritParticleMessage::handle);
    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        e.enqueueWork(
                () -> {
                    MobEffects.BLINDNESS.value().addAttributeModifier(
                            Attributes.FOLLOW_RANGE,
                            loc("blindness_follow_range"),
                            -0.75,
                            Operation.ADD_MULTIPLIED_TOTAL);
                });
    }

    // TODO - Update impls to reflect new default values.
    @SubscribeEvent
    public void applyAttribs(EntityAttributeModificationEvent e) {
        e.getTypes()
                .forEach(
                        type -> {
                            addAll(
                                    type,
                                    e::add,
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
                        });
    }

    @SafeVarargs
    private static void addAll(
            EntityType<? extends LivingEntity> type,
            BiConsumer<EntityType<? extends LivingEntity>, Holder<Attribute>> add,
            RegistryObject<? extends Attribute>... attribs) {
        for (RegistryObject<? extends Attribute> a : attribs) addOne(type, add, a);
    }

    @SuppressWarnings("unchecked")
    private static void addOne(
            EntityType<? extends LivingEntity> type,
            BiConsumer<EntityType<? extends LivingEntity>, Holder<Attribute>> add,
            RegistryObject<? extends Attribute> attrib) {
        add.accept(type, (Holder<Attribute>) (Holder<?>) attrib.asHolder());
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        AttributeSupplier playerAttribs = DefaultAttributes.getSupplier(EntityType.PLAYER);
        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            if (playerAttribs.hasAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr))) {
                attr.setSyncable(true);
            }
        }
    }

    public static TooltipFlag getTooltipFlag() {
        if (FMLEnvironment.dist.isClient()) return ClientAccess.getTooltipFlag();
        return TooltipFlag.NORMAL;
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static class ClientAccess {
        static TooltipFlag getTooltipFlag() {
            return Minecraft.getInstance().options.advancedItemTooltips
                    ? TooltipFlag.ADVANCED
                    : TooltipFlag.NORMAL;
        }
    }
}
