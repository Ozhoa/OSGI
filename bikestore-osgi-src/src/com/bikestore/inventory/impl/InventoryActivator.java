package com.bikestore.inventory.impl;
import com.bikestore.inventory.api.InventoryService;
import com.bikestore.osgi.*;
import java.time.Instant;
import java.util.Properties;
/**
 * BundleActivator del InventoryBundle.
 * start() -> registra InventoryService en el ServiceRegistry del framework.
 * stop()  -> elimina el servicio -> OrderBundle entra en modo degradado.
 *
 * Comando Karaf equivalente:
 *   karaf> bundle:start com.bikestore.inventory.bundle
 *   karaf> bundle:stop  com.bikestore.inventory.bundle
 */
public class InventoryActivator implements BundleActivator {
    private ServiceRegistration<InventoryService> registration;
    public void start(BundleContext ctx) {
        log("=== BUNDLE STARTING ===");
        InventoryServiceImpl svc = new InventoryServiceImpl();
        Properties props = new Properties();
        props.setProperty("service.description","BikeStore Inventory Service");
        props.setProperty("bundle.version","1.0.0");
        registration = ctx.registerService(InventoryService.class, svc, props);
        log("InventoryService registrado | service.id=" + registration.getReference().getProperty("service.id"));
        log("=== BUNDLE ACTIVE ===");
    }
    public void stop(BundleContext ctx) {
        log("=== BUNDLE STOPPING ===");
        if(registration!=null){ registration.unregister(); registration=null;
            log("InventoryService retirado del ServiceRegistry"); }
        log("=== BUNDLE STOPPED ===");
    }
    private void log(String m){ System.out.printf("[%s] [InventoryActivator] %s%n",Instant.now(),m); }
}
