package dev.shadowsoffire.placebo.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 1.12.2 registration facade — deferred-register-ish helper without {@code RegistryObject}.
 * <p>
 * For real Forge registries (Potion, SoundEvent) the entries are queued and registered when the
 * corresponding {@link RegistryEvent.Register} fires. For non-registry attribute instances
 * ({@link IAttribute}) the entries are stored directly and exposed via {@link #getAttributes()}
 * so a mixin can apply them to every {@code EntityLivingBase} at attribute-registration time.
 */
public class DeferredHelper {

    protected final String modid;
    private final List<Registrar<Potion>> potions = new ArrayList<>();
    private final List<Registrar<SoundEvent>> sounds = new ArrayList<>();
    private final Map<ResourceLocation, IAttribute> attributes = new LinkedHashMap<>();

    /**
     * Creates a helper and subscribes it to the Forge event bus so the registry events can be
     * handled.
     */
    public static DeferredHelper create(String modid) {
        DeferredHelper helper = new DeferredHelper(modid);
        MinecraftForge.EVENT_BUS.register(helper);
        return helper;
    }

    protected DeferredHelper(String modid) {
        this.modid = modid;
    }

    public <T extends Potion> RegObj<T> effect(String path, Supplier<T> factory) {
        ResourceLocation id = new ResourceLocation(this.modid, path);
        RegObj<T> obj = new RegObj<>(id);
        this.potions.add(new Registrar<>(id, obj, factory::get));
        return obj;
    }

    public <T extends SoundEvent> RegObj<T> sound(String path, Supplier<T> factory) {
        ResourceLocation id = new ResourceLocation(this.modid, path);
        RegObj<T> obj = new RegObj<>(id);
        this.sounds.add(new Registrar<>(id, obj, factory::get));
        return obj;
    }

    public RegObj<SoundEvent> sound(String path) {
        return this.sound(path, () -> new SoundEvent(new ResourceLocation(this.modid, path)));
    }

    /**
     * In 1.12.2 attributes are not forge-registry entries. We keep an ordered map and let an
     * {@code EntityLivingBase} mixin register every kept attribute onto each entity's attribute
     * map at construction time.
     */
    public <T extends IAttribute> RegObj<T> attribute(String path, Supplier<T> factory) {
        ResourceLocation id = new ResourceLocation(this.modid, path);
        T attr = factory.get();
        this.attributes.put(id, attr);
        return new RegObj<>(id, attr);
    }

    public Collection<IAttribute> getAttributes() {
        return Collections.unmodifiableCollection(this.attributes.values());
    }

    @SubscribeEvent
    public void onRegisterPotions(RegistryEvent.Register<Potion> e) {
        for (Registrar<Potion> r : this.potions) {
            Potion entry = r.factory.get();
            if (entry.getRegistryName() == null) {
                entry.setRegistryName(r.id);
            }
            e.getRegistry().register(entry);
            r.obj.value = entry;
        }
    }

    @SubscribeEvent
    public void onRegisterSounds(RegistryEvent.Register<SoundEvent> e) {
        for (Registrar<SoundEvent> r : this.sounds) {
            SoundEvent entry = r.factory.get();
            if (entry.getRegistryName() == null) {
                entry.setRegistryName(r.id);
            }
            e.getRegistry().register(entry);
            r.obj.value = entry;
        }
    }

    /** A minimal holder replacing 1.16's {@code RegistryObject} — exposes {@code get()} only. */
    public static final class RegObj<T> {
        private final ResourceLocation id;
        T value;

        public RegObj(ResourceLocation id) {
            this.id = id;
        }

        public RegObj(ResourceLocation id, T value) {
            this.id = id;
            this.value = value;
        }

        public T get() {
            if (this.value == null) {
                throw new IllegalStateException("Registry entry " + this.id + " is not registered yet");
            }
            return this.value;
        }

        public ResourceLocation getId() {
            return this.id;
        }
    }

    private static final class Registrar<T> {
        final ResourceLocation id;
        final RegObj<T> obj;
        final Supplier<T> factory;

        Registrar(ResourceLocation id, RegObj<T> obj, Supplier<T> factory) {
            this.id = id;
            this.obj = obj;
            this.factory = factory;
        }
    }
}
