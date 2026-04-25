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
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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

    public <T extends Effect> RegistryObject<T> effect(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.POTIONS.getRegistrySuperType(), factory);
    }

    public <T extends SoundEvent> RegistryObject<T> sound(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.SOUND_EVENTS.getRegistrySuperType(), factory);
    }

    public RegistryObject<SoundEvent> sound(String path) {
        return sound(path, () -> new SoundEvent(new ResourceLocation(modid, path)));
    }

    public <T extends Potion> RegistryObject<T> potion(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.POTION_TYPES.getRegistrySuperType(), factory);
    }

    public <T extends Enchantment> RegistryObject<T> enchant(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.ENCHANTMENTS.getRegistrySuperType(), factory);
    }

    public <U extends Entity, T extends EntityType<U>> RegistryObject<T> entity(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.ENTITIES.getRegistrySuperType(), factory);
    }

    //    public <U extends BlockEntity, T extends BlockEntityType<U>> RegistryObject<T>
    // blockEntity(
    //            String path, Supplier<T> factory) {
    //        return this.create(path, ForgeRegistries.BLOCK_ENTITIES.getRegistrySuperType(),
    // factory);
    //    }

    public <U extends IParticleData, T extends ParticleType<U>> RegistryObject<T> particle(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.PARTICLE_TYPES.getRegistrySuperType(), factory);
    }

    //    public <U extends AbstractContainerMenu, T extends MenuType<U>> RegistryObject<T> menu(
    //            String path, Supplier<T> factory) {
    //        return this.create(path, ForgeRegistries.Keys.MENU, factory);
    //    }

    public <T extends PaintingType> RegistryObject<T> painting(String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.PAINTING_TYPES.getRegistrySuperType(), factory);
    }

    public <C extends IInventory, U extends IRecipe<C>, T extends IRecipeSerializer<U>>
            RegistryObject<T> recipeSerializer(String path, Supplier<T> factory) {
        return this.create(
                path, ForgeRegistries.RECIPE_SERIALIZERS.getRegistrySuperType(), factory);
    }

    public <S, U extends StatType<S>, T extends StatType<U>> RegistryObject<T> stat(
            String path, Supplier<T> factory) {
        return this.create(path, ForgeRegistries.STAT_TYPES.getRegistrySuperType(), factory);
    }

    //    /**
    //     * Creates a custom stat with the given path and formatter.<br>
    //     * Calling {@link StatType#get} on {@link Stats#CUSTOM} is required for full registration,
    // for
    //     * some reason.
    //     *
    //     * @see Stats#makeCustomStat
    //     */
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

    public <U extends IFeatureConfig, T extends Feature<U>> RegistryObject<T> feature(
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
        return this.create(path, ForgeRegistries.ATTRIBUTES.getRegistrySuperType(), factory);
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

    protected class Registrar<T extends IForgeRegistryEntry<T>, U extends T> {
        private final ResourceLocation id;
        private final RegistryObject<U> obj;
        private final IForgeRegistryEntrySupplier<T, U> factory;

        public Registrar(
                ResourceLocation id,
                RegistryObject<U> obj,
                IForgeRegistryEntrySupplier<T, U> factory) {
            this.id = id;
            this.obj = obj;
            this.factory = factory;
        }

        public ResourceLocation getId() {
            return id;
        }

        public RegistryObject<U> getObj() {
            return obj;
        }

        public IForgeRegistryEntrySupplier<T, U> getFactory() {
            return factory;
        }
    }
}
