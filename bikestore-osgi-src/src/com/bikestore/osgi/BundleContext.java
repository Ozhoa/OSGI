package com.bikestore.osgi;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class BundleContext {
    private static final BundleContext INSTANCE = new BundleContext();
    public static BundleContext getInstance() { return INSTANCE; }
    private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();
    private final Map<Class<?>, Properties> serviceProps = new ConcurrentHashMap<>();
    private long nextServiceId = 1;
    public <T> ServiceRegistration<T> registerService(Class<T> clazz, T impl, Properties props) {
        Properties p = (props != null) ? props : new Properties();
        p.setProperty("service.id", String.valueOf(nextServiceId++));
        registry.put(clazz, impl);
        serviceProps.put(clazz, p);
        return new ServiceRegistration<>(clazz, this);
    }
    public <T> ServiceReference<T> getServiceReference(Class<T> clazz) {
        if (!registry.containsKey(clazz)) return null;
        return new ServiceReference<>(clazz, serviceProps.get(clazz));
    }
    @SuppressWarnings("unchecked")
    public <T> T getService(ServiceReference<T> ref) {
        return (T) registry.get(ref.getServiceClass());
    }
    public <T> void ungetService(ServiceReference<T> ref) {}
    public void unregister(Class<?> clazz) { registry.remove(clazz); serviceProps.remove(clazz); }
    public boolean isRegistered(Class<?> clazz) { return registry.containsKey(clazz); }
}
