package com.hyrhythm.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class DependencyLoader {
    private static final Logger LOGGER = Logger.getLogger(DependencyLoader.class.getName());
    private static final DependencyLoader FALLBACK_DEPENDENCY_LOADER = new DependencyLoader();

    private final ConcurrentMap<Class<?>, Object> dependencies = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<Class<?>> loadOrder = new ConcurrentLinkedDeque<>();

    public <T> void registerInstance(Class<T> type, T instance) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(instance, "instance");

        ensureAssignable(type, instance, "registerInstance");
        Object existing = dependencies.putIfAbsent(type, instance);
        if (existing == null) {
            rememberLoadOrder(type);
            return;
        }
        if (existing instanceof Supplier<?>) {
            dependencies.put(type, instance);
            return;
        }
        fail("registerInstance: instance already registered: " + type.getName());
    }

    @SuppressWarnings("unchecked")
    public void registerImportantInstance(Class<?> type, Object instance) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(instance, "instance");

        ensureAssignable(type, instance, "registerImportantInstance");
        registerInstance((Class<Object>) type, instance);
    }

    public void loadQueuedDependenciesInOrder() {
        for (Class<?> type : loadOrder) {
            Object value = dependencies.get(type);
            if (!(value instanceof Supplier<?> supplier)) {
                continue;
            }

            Object instance = Objects.requireNonNull(supplier.get(), "supplier returned null");
            ensureAssignable(type, instance, "loadQueuedDependenciesInOrder");
            boolean replaced = dependencies.replace(type, value, instance);
            if (!replaced) {
                fail("loadQueuedDependenciesInOrder: dependency changed concurrently: " + type.getName());
            }
        }
    }

    public static DependencyLoader getFallbackDependencyLoader() {
        return FALLBACK_DEPENDENCY_LOADER;
    }

    public <T> T requireInstance(Class<T> type) {
        Objects.requireNonNull(type, "type");

        Object value = dependencies.get(type);
        if (value == null) {
            fail("requireInstance: missing instance: " + type.getName());
        }
        if (value instanceof Supplier<?>) {
            fail("requireInstance: dependency queued but not loaded: " + type.getName());
        }
        return type.cast(value);
    }

    public Collection<Object> getAllInstances() {
        return new ArrayList<>(dependencies.values());
    }

    public <T> T replaceInstance(Class<T> type, Supplier<? extends T> supplier) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(supplier, "supplier");

        Object existing = dependencies.get(type);
        if (existing == null || existing instanceof Supplier<?>) {
            fail("replaceInstance: instance not registered: " + type.getName());
        }

        T instance = Objects.requireNonNull(supplier.get(), "supplier returned null");
        ensureAssignable(type, instance, "replaceInstance");

        boolean replaced = dependencies.replace(type, existing, instance);
        if (!replaced) {
            fail("replaceInstance: instance changed concurrently: " + type.getName());
        }
        return instance;
    }

    public void resetInstances() {
        dependencies.clear();
        loadOrder.clear();
    }

    public void queueDependency(Class<?> type, Supplier<?> supplier) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(supplier, "supplier");

        Object existing = dependencies.putIfAbsent(type, supplier);
        if (existing == null) {
            rememberLoadOrder(type);
            return;
        }
        fail("queueDependency: dependency already queued: " + type.getName());
    }

    public boolean isInstanceRegistered(Class<?> type) {
        if (type == null) {
            return false;
        }

        Object value = dependencies.get(type);
        return value != null && !(value instanceof Supplier<?>);
    }

    private static void ensureAssignable(Class<?> type, Object value, String action) {
        boolean isAssignable = type.isAssignableFrom(value.getClass());
        if (!isAssignable) {
            fail(action + ": type mismatch. expected=" + type.getName() + " got=" + value.getClass().getName());
        }
    }

    private void rememberLoadOrder(Class<?> type) {
        loadOrder.addLast(type);
    }

    private static void fail(String message) {
        LOGGER.severe("[HyRhythm] [DependencyLoader] " + message);
        throw new IllegalStateException(message);
    }
}
