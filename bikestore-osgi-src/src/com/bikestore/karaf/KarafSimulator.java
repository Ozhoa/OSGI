package com.bikestore.karaf;

import com.bikestore.inventory.impl.InventoryActivator;
import com.bikestore.order.OrderActivator;
import com.bikestore.order.OrderService;
import com.bikestore.osgi.BundleContext;
import java.time.Instant;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  KarafSimulator — Simula el contenedor OSGi Apache Karaf
 *
 *  Arquitectura demostrada:
 *  ┌─────────────────────────────────────────────────┐
 *  │  Apache Karaf (OSGi Container)                  │
 *  │  ┌────────────────┐  ┌──────────────────────┐   │
 *  │  │ inventory-api  │  │  inventory-bundle    │   │
 *  │  │ (Export-Pkg)   │  │  (BundleActivator)   │   │
 *  │  └────────┬───────┘  └──────────────────────┘   │
 *  │           │ ServiceRegistry (OSGi)               │
 *  │  ┌────────▼───────────────────────────────────┐  │
 *  │  │  order-bundle (consume via ServiceRef)     │  │
 *  │  └────────────────────────────────────────────┘  │
 *  └─────────────────────────────────────────────────┘
 *
 *  Patron principal: Service Locator + Dependency Inversion
 *  Permite: arrancar/parar bundles sin afectar el sistema
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class KarafSimulator {

    private static final BundleContext CTX = BundleContext.getInstance();
    private static InventoryActivator inventoryActivator;
    private static OrderActivator     orderActivator;
    private static boolean invRunning   = false;
    private static boolean orderRunning = false;

    public static void main(String[] args) throws Exception {
        sep('═', 70);
        System.out.println("  BikeStore OSGi — Apache Karaf Container Simulator");
        System.out.println("  Patron: Service Registry + Dynamic Bundle Lifecycle");
        sep('═', 70);
        sleep(300);

        // ── Escenario 1: Arranque normal ─────────────────────
        sep('-', 70);
        karafCmd("bundle:start com.bikestore.inventory.bundle");
        sep('-', 70);
        bundleStart(true);
        sleep(300);

        sep('-', 70);
        karafCmd("bundle:start com.bikestore.order.bundle");
        sep('-', 70);
        bundleStart(false);
        sleep(300);

        bundleList();

        // ── Operacion normal ──────────────────────────────────
        sep('─', 70);
        demo("=== ESCENARIO 1: Ambos bundles ACTIVE ===");
        sep('─', 70);
        OrderService orders = orderActivator.getOrderService();
        orders.mostrarCatalogo();
        sleep(200);

        String r1 = orders.crearPedido("Ana Garcia",   "B001", 1);
        String r2 = orders.crearPedido("Carlos Perez", "B003", 2);
        String r3 = orders.crearPedido("Maria Lopez",  "B002", 1);
        String r4 = orders.crearPedido("Luis Mora",    "B004", 5); // stock insuficiente
        demo("Resultados: "+r1+" | "+r2+" | "+r3+" | "+r4);
        demo("Dependencia: " + orders.estadoDependencia());
        sleep(500);

        // ── Escenario 2: Detener InventoryBundle ─────────────
        sep('-', 70);
        karafCmd("bundle:stop com.bikestore.inventory.bundle");
        karafCmd("  -> OrderBundle debe continuar en modo degradado...");
        sep('-', 70);
        bundleStop(true);
        sleep(300);

        sep('─', 70);
        demo("=== ESCENARIO 2: InventoryBundle STOPPED ===");
        demo("OrderBundle sigue ACTIVO — sin excepciones");
        sep('─', 70);
        demo("Estado dependencia: " + orders.estadoDependencia());
        String r5 = orders.crearPedido("Rosa Diaz", "B001", 1);
        String r6 = orders.crearPedido("Juan Ruiz", "B002", 2);
        demo("Pedidos con bundle detenido: "+r5+" | "+r6);
        sleep(500);

        // ── Escenario 3: Re-arrancar InventoryBundle ─────────
        sep('-', 70);
        karafCmd("bundle:start com.bikestore.inventory.bundle  (re-deploy)");
        sep('-', 70);
        bundleStart(true);
        sleep(300);

        sep('─', 70);
        demo("=== ESCENARIO 3: InventoryBundle RE-ARRANCADO ===");
        demo("Recuperacion AUTOMATICA — sin reiniciar OrderBundle");
        sep('─', 70);
        demo("Estado dependencia: " + orders.estadoDependencia());
        String r7 = orders.crearPedido("Ana Garcia",  "B003", 1);
        String r8 = orders.crearPedido("Pedro Vega",  "B001", 2);
        demo("Pedidos post-recuperacion: "+r7+" | "+r8);
        sleep(300);

        // ── bundle:list final ─────────────────────────────────
        bundleList();

        // ── Shutdown ──────────────────────────────────────────
        sep('-', 70);
        karafCmd("shutdown -f  (deteniendo todos los bundles)");
        sep('-', 70);
        bundleStop(false);
        bundleStop(true);

        sep('═', 70);
        System.out.println("  Framework detenido correctamente. Todos los bundles: RESOLVED.");
        sep('═', 70);
    }

    // ── Helpers de ciclo de vida ──────────────────────────────

    private static void bundleStart(boolean inventory) throws Exception {
        if (inventory) {
            inventoryActivator = new InventoryActivator();
            inventoryActivator.start(CTX);
            invRunning = true;
        } else {
            orderActivator = new OrderActivator();
            orderActivator.start(CTX);
            orderRunning = true;
        }
    }

    private static void bundleStop(boolean inventory) throws Exception {
        if (inventory && invRunning) {
            inventoryActivator.stop(CTX);
            invRunning = false;
        } else if (!inventory && orderRunning) {
            orderActivator.stop(CTX);
            orderRunning = false;
        }
    }

    private static void bundleList() {
        sep('─', 70);
        karafCmd("bundle:list");
        sep('─', 70);
        System.out.printf("  %-5s %-10s %-8s %-35s%n","ID","State","Level","Name");
        System.out.println("  " + "-".repeat(62));
        System.out.printf("  %-5s %-10s %-8s %-35s%n",
            "[1]","RESOLVED","30","com.bikestore.inventory.api");
        System.out.printf("  %-5s %-10s %-8s %-35s%n",
            "[2]", invRunning?"ACTIVE":"RESOLVED","30","com.bikestore.inventory.bundle");
        System.out.printf("  %-5s %-10s %-8s %-35s%n",
            "[3]", orderRunning?"ACTIVE":"RESOLVED","30","com.bikestore.order.bundle");
        System.out.println();
    }

    private static void karafCmd(String cmd) {
        System.out.printf("[%s] [karaf@bikestore] > %s%n", Instant.now(), cmd);
    }
    private static void demo(String msg) {
        System.out.printf("[%s] [DEMO] %s%n", Instant.now(), msg);
    }
    private static void sep(char c, int n)  { System.out.println(String.valueOf(c).repeat(n)); }
    private static void sleep(long ms) throws InterruptedException { Thread.sleep(ms); }
}
