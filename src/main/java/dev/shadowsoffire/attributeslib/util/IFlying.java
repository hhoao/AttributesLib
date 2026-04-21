package dev.shadowsoffire.attributeslib.util;

/**
 * Caches and manipulates the value of {@code PlayerCapabilities#isFlying} so the flag isn't dropped
 * on respawn or login.
 */
public interface IFlying {

    /**
     * Returns whether the user was flying the last time {@code PlayerCapabilities} was read from
     * disk, then clears the cached value so future calls return false.
     */
    boolean getAndDestroyFlyingCache();

    /** Marks the player as previously flying so the capability state is restored. */
    void markFlying();
}
