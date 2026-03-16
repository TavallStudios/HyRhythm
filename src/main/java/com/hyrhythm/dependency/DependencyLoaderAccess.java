package com.hyrhythm.dependency;

import com.hyrhythm.bootstrap.PluginBootstrap;

import java.util.Objects;

public final class DependencyLoaderAccess {
    private DependencyLoaderAccess() {
    }

    private static DependencyLoader loader() {
        PluginBootstrap activePluginBootstrap = PluginBootstrap.getActivePluginBootstrap();
        if (activePluginBootstrap != null) {
            return activePluginBootstrap.getDependencyLoader();
        }
        return DependencyLoader.getFallbackDependencyLoader();
    }

    public static <T> T findInstance(Class<T> type) {
        DependencyLoader activeLoader = loader();
        boolean isRegistered = activeLoader.isInstanceRegistered(type);
        if (!isRegistered) {
            return null;
        }
        return activeLoader.requireInstance(type);
    }

    public static <T> T requireInstance(Class<T> type, String message) {
        T instance = findInstance(type);
        return Objects.requireNonNull(instance, message);
    }
}
