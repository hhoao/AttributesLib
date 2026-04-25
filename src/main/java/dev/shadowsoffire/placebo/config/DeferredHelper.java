package dev.shadowsoffire.placebo.config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

public class DeferredHelper {

    protected final String modid;
    protected final Map<Class<? extends IForgeRegistryEntry<?>>, List<Registrar<?, ?>>> objects;

    /**
     * Creates a new DeferredHelper and registers it to the mod event bus.
     *
     * @param modid The modid of the owning mod.
     * @return A new DeferredHelper.
     */
    public static DeferredHelper create(String modid) {
        DeferredHelper helper = new DeferredHelper(modid);
        FMLJavaModLoadingContext.get().getModEventBus().register(helper);
        return helper;
    }

    protected DeferredHelper(String modid) {
        this.modid = modid;
        this.objects = new IdentityHashMap<>();
    }

    public <T extends Block> RegistryObject<T> block(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.BLOCKS.getRegistrySuperType(), factory);
    }

    public <T extends Fluid> RegistryObject<T> fluid(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.FLUIDS.getRegistrySuperType(), factory);
    }

    public <T extends Item> RegistryObject<T> item(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.ITEMS.getRegistrySuperType(), factory);
    }

    public <T extends MobEffect> RegistryObject<T> effect(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.MOB_EFFECTS.getRegistrySuperType(), factory);
    }

    public <T extends SoundEvent> RegistryObject<T> sound(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.SOUND_EVENTS.getRegistrySuperType(), factory);
    }

    public RegistryObject<SoundEvent> sound(String path) {

        return sound(path, () -> new SoundEvent(new ResourceLocation(modid, path)));
    }

    public <T extends Potion> RegistryObject<T> potion(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.POTIONS.getRegistrySuperType(), factory);
    }

    public <T extends Enchantment> RegistryObject<T> enchant(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.ENCHANTMENTS.getRegistrySuperType(), factory);
    }

    public <U extends Entity, T extends EntityType<U>> RegistryObject<T> entity(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.ENTITIES.getRegistrySuperType(), factory);
    }

    public <U extends BlockEntity, T extends BlockEntityType<U>> RegistryObject<T> blockEntity(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.BLOCK_ENTITIES.getRegistrySuperType(), factory);
    }

    public <U extends ParticleOptions, T extends ParticleType<U>> RegistryObject<T> particle(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.PARTICLE_TYPES.getRegistrySuperType(), factory);
    }

    //    public <U extends AbstractContainerMenu, T extends MenuType<U>> RegistryObject<T> menu(
    //            String path, Supplier<T> factory) {
    //        return this.create(path, ForgeRegistries.Keys.MENU, factory);
    //    }

    public <T extends Motive> RegistryObject<T> painting(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.PAINTING_TYPES.getRegistrySuperType(), factory);
    }

    public <C extends Container, U extends Recipe<C>, T extends RecipeSerializer<U>>
            RegistryObject<T> recipeSerializer(String path, Supplier<T> factory) {
        return this.create(
                path, ForgeRegistries.RECIPE_SERIALIZERS.getRegistrySuperType(), factory);
    }

    public <S, U extends StatType<S>, T extends StatType<U>> RegistryObject<T> stat(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.STAT_TYPES.getRegistrySuperType(), factory);
    }

    /**
     * Creates a custom stat with the given path and formatter.<br>
     * Calling {@link StatType#get} on {@link Stats#CUSTOM} is required for full registration, for
     * some reason.
     *
     * @see Stats#makeCustomStat
     */
    //    public RegistryObject<ResourceLocation> customStat(String path, StatFormatter formatter) {
    //        ResourceLocation registryName = Registry.CUSTOM_STAT_REGISTRY.getRegistryName();
    //        Registry.CUSTOM_STAT
    //        return this.create(
    //                path,
    //            Registry.CUSTOM_STAT_REGISTRY,
    //                () -> {
    //                    ResourceLocation id = new ResourceLocation(this.modid, path);
    //                    Stats.CUSTOM.get(id, formatter);
    //                    return id;
    //                });
    //    }

    public <U extends FeatureConfiguration, T extends Feature<U>> RegistryObject<T> feature(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.FEATURES.getRegistrySuperType(), factory);
    }

    //    public <T extends CreativeModeTab> RegistryObject<T> tab(String path, Supplier<T> factory)
    // {
    //        return this.create(path, ForgeRegistries.CREATIVE_MODE_TAB, factory);
    //    }

    //    public <P, T extends P> RegistryObject<T> custom(
    //            String path, ResourceKey<Registry<P>> registry, Supplier<T> factory) {
    //        return this.create(path, registry, factory);
    //    }

    //    public <U extends ParticleOptions, T extends ParticleType<U>> RegistryObject<T> particle(
    //        String path, Supplier<T> factory) {
    //        return this.create(path, ParticleType.class, factory);
    //    }

    public <T extends Attribute> RegistryObject<T> attribute(String path, Supplier<T> factory) {
        Class<Attribute> registrySuperType = ForgeRegistries.ATTRIBUTES.getRegistrySuperType();
        return this.create(path, registrySuperType, factory);
    }

    protected <P extends IForgeRegistryEntry<P>, T extends P> RegistryObject<T> create(
            String path, Class<P> regKey, Supplier<T> factory) {
        List<Registrar<?, ?>> registrars =
                this.objects.computeIfAbsent(regKey, k -> new ArrayList<>());
        ResourceLocation id = new ResourceLocation(this.modid, path);

        RegistryObject<T> obj = RegistryObject.of(id, regKey, modid);
        registrars.add(new Registrar<>(id, obj, new IForgeRegistryEntrySupplier<>(id, factory)));
        return obj;
    }

    private static MethodHandle RO_updateReference;

    static {
        try {
            Method m =
                    RegistryObject.class.getDeclaredMethod("updateReference", IForgeRegistry.class);
            m.setAccessible(true);
            RO_updateReference = MethodHandles.lookup().unreflect(m);
        } catch (Exception ex) {
            // Failing means we're using Neo, and RO has been replaced with DH, so this is
            // unnecessary anyway.
        }
    }

    public static class IForgeRegistryEntrySupplier<T extends IForgeRegistryEntry<T>, U extends T> {
        private final ResourceLocation resourceLocation;
        private final Supplier<U> entry;

        public IForgeRegistryEntrySupplier(ResourceLocation resourceLocation, Supplier<U> entry) {
            this.resourceLocation = resourceLocation;
            this.entry = entry;
        }

        public U get() {
            U t = entry.get();
            t.setRegistryName(resourceLocation);
            return t;
        }
    }

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends IForgeRegistryEntry<T>> void register(RegistryEvent.Register e) {
        this.objects
                .getOrDefault(e.getRegistry().getRegistrySuperType(), Collections.emptyList())
                .forEach(
                        registrar -> {
                            IForgeRegistry<T> registry = e.getRegistry();
                            registry.register((T) registrar.factory.get());
                            if (RO_updateReference != null) {
                                try {
                                    RO_updateReference.invoke(registrar.obj, registry);
                                } catch (Throwable t) {
                                    throw new RuntimeException(t);
                                }
                            }
                        });
    }

    protected static record Registrar<T extends IForgeRegistryEntry<T>, U extends T>(
            ResourceLocation id,
            RegistryObject<U> obj,
            IForgeRegistryEntrySupplier<T, U> factory) {}
}
