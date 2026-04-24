package dev.shadowsoffire.placebo.config;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Small compatibility wrapper around NeoForge's {@link DeferredHolder}.
 *
 * <p>The 1.20 codebase used Forge's RegistryObject pervasively. Keeping this local wrapper lets the
 * port move to NeoForge registration without forcing unrelated gameplay code to churn at the same
 * time.
 */
public final class RegistryObject<T> implements Supplier<T> {

    private final DeferredHolder<?, ?> holder;

    private RegistryObject(DeferredHolder<?, ?> holder) {
        this.holder = holder;
    }

    public static <R, T extends R> RegistryObject<T> create(DeferredHolder<R, T> holder) {
        return new RegistryObject<>(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.holder.get();
    }

    public Optional<T> asOptional() {
        return this.holder.asOptional().map(value -> (T) value);
    }

    public ResourceLocation getId() {
        return this.holder.getId();
    }

    public ResourceKey<?> getKey() {
        return this.holder.getKey();
    }

    @SuppressWarnings("unchecked")
    public Holder<T> asHolder() {
        return (Holder<T>) this.holder;
    }

    public boolean isBound() {
        return this.holder.isBound();
    }
}
