package eu.zunix.ore_and_alloy.integration;

import java.lang.reflect.Method;

final class ModListAccessor {
    private final Object modListInstance;
    private final Method isLoadedMethod;

    private ModListAccessor(Object modListInstance, Method isLoadedMethod) {
        this.modListInstance = modListInstance;
        this.isLoadedMethod = isLoadedMethod;
    }

    static ModListAccessor resolve() {
        try {
            Class<?> modListClass = Class.forName("net.neoforged.fml.ModList");
            Method get = modListClass.getMethod("get");
            Object instance = get.invoke(null);
            Method isLoaded = instance.getClass().getMethod("isLoaded", String.class);
            return new ModListAccessor(instance, isLoaded);
        } catch (Throwable ignored) {
            return new ModListAccessor(null, null);
        }
    }

    boolean available() {
        return modListInstance != null && isLoadedMethod != null;
    }

    boolean isLoaded(String modId) {
        if (!available()) return false;
        try {
            Object loaded = isLoadedMethod.invoke(modListInstance, modId);
            return loaded instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
