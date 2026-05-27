package com.bikestore.order;
import com.bikestore.osgi.*;
import java.time.Instant;
/**
 * BundleActivator del OrderBundle.
 * Puede arrancarse/pararse independientemente del InventoryBundle.
 * El sistema sigue operativo en todo momento.
 */
public class OrderActivator implements BundleActivator {
    private OrderService orderService;
    public void start(BundleContext ctx) {
        log("=== BUNDLE STARTING ===");
        orderService = new OrderService(ctx);
        log("OrderService listo | dependencia dinamica: InventoryService");
        log("=== BUNDLE ACTIVE ===");
    }
    public void stop(BundleContext ctx) {
        log("=== BUNDLE STOPPING ===");
        if(orderService!=null){
            log("Historial de pedidos:");
            orderService.getHistorial().forEach(h -> log("  "+h));
            orderService = null;
        }
        log("=== BUNDLE STOPPED ===");
    }
    public OrderService getOrderService() { return orderService; }
    private void log(String m){ System.out.printf("[%s] [OrderActivator] %s%n",Instant.now(),m); }
}
