package com.bikestore.order;
import com.bikestore.inventory.api.InventoryService;
import com.bikestore.osgi.*;
import java.time.Instant;
import java.util.*;
/**
 * Servicio de pedidos con BAJO ACOPLAMIENTO OSGi.
 *
 * Patron: Dynamic Service Lookup (ServiceRegistry pattern)
 * - No tiene campo "private InventoryService inventario" fijo.
 * - Cada operacion consulta el ServiceRegistry en tiempo de ejecucion.
 * - Si el InventoryBundle esta detenido -> null -> modo degradado.
 * - Si vuelve a arrancar -> recuperacion automatica sin reiniciar OrderBundle.
 */
public class OrderService {
    private final BundleContext context;
    private final List<String> historial = new ArrayList<>();

    public OrderService(BundleContext ctx) { this.context = ctx; }

    public String crearPedido(String cliente, String bikeId, int cantidad) {
        String pedidoId = "PED-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        log("Procesando "+pedidoId+" | Cliente: "+cliente+" | Bike: "+bikeId+" x"+cantidad);
        InventoryService inv = obtener();
        if (inv == null) {
            String msg = pedidoId+" | MODO DEGRADADO: InventoryService no disponible";
            historial.add("[PENDIENTE] "+msg);
            log("ADVERTENCIA: "+msg);
            return "PENDIENTE:"+pedidoId;
        }
        if (!inv.hayStock(bikeId, cantidad)) {
            String msg = pedidoId+" | RECHAZADO: stock insuficiente para "+bikeId;
            historial.add("[RECHAZADO] "+msg);
            log(msg); liberar(inv); return "RECHAZADO:"+pedidoId;
        }
        boolean ok = inv.reservarStock(bikeId, cantidad);
        liberar(inv);
        if (ok) { String msg = pedidoId+" | CONFIRMADO | "+cliente+" | "+bikeId+" x"+cantidad;
            historial.add("[CONFIRMADO] "+msg); log("Pedido CONFIRMADO: "+pedidoId); return "CONFIRMADO:"+pedidoId; }
        return "ERROR:"+pedidoId;
    }

    public void mostrarCatalogo() {
        InventoryService inv = obtener();
        if (inv == null) { log("Catalogo no disponible (InventoryBundle detenido)"); return; }
        log("=== CATALOGO BIKESTORE ===");
        inv.listarCatalogo().forEach(b ->
            log(String.format("  [%s] %-35s $%8.2f  stock:%d", b.getId(), b.getModelo(), b.getPrecio(), b.getStock())));
        liberar(inv);
    }

    public String estadoDependencia() {
        InventoryService inv = obtener();
        if (inv == null) return "InventoryService: NO DISPONIBLE (bundle detenido)";
        String s = inv.getStatus(); liberar(inv); return s;
    }

    public List<String> getHistorial() { return Collections.unmodifiableList(historial); }

    private InventoryService obtener() {
        try {
            ServiceReference<InventoryService> ref = context.getServiceReference(InventoryService.class);
            return ref == null ? null : context.getService(ref);
        } catch(Exception e) { log("Error obteniendo servicio: "+e.getMessage()); return null; }
    }
    private void liberar(InventoryService svc) {
        try { ServiceReference<InventoryService> r = context.getServiceReference(InventoryService.class);
            if(r!=null) context.ungetService(r); } catch(Exception ignored){}
    }
    private void log(String m){ System.out.printf("[%s] [OrderBundle] %s%n",Instant.now(),m); }
}
