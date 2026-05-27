package com.bikestore.osgi;
public class ServiceRegistration<T> {
    private final Class<T> clazz;
    private final BundleContext ctx;
    public ServiceRegistration(Class<T> c, BundleContext ctx) { this.clazz=c; this.ctx=ctx; }
    public void unregister() { ctx.unregister(clazz); }
    public ServiceReference<T> getReference() { return new ServiceReference<>(clazz, null); }
}
