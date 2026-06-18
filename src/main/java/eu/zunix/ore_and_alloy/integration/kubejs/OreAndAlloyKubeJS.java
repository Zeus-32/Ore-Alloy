package eu.zunix.ore_and_alloy.integration.kubejs;

import java.util.Objects;
import java.util.function.Consumer;

public final class OreAndAlloyKubeJS {
    private OreAndAlloyKubeJS() {}

    public static void registry(Consumer<OreAndAlloyRegistryEvent> callback) {
        Objects.requireNonNull(callback, "OreAndAlloy.registry callback")
                .accept(new OreAndAlloyRegistryEvent());
    }
}
