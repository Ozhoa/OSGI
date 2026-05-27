package com.bikestore.inventory.impl;
import com.bikestore.inventory.api.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Implementacion PRIVADA del InventoryService.
 * Paquete: com.bikestore.inventory.impl  (NO exportado)
 * OrderBundle NUNCA puede importar esta clase directamente.
 */
public class InventoryServiceImpl implements InventoryService {
    private final Map<String, Bike> catalogo = new ConcurrentHashMap<>();
    public InventoryServiceImpl() {
        catalogo.put("B001", new Bike("B001","Trek Marlin 7",         1299.99, 5));
        catalogo.put("B002", new Bike("B002","Specialized Rockhopper",  999.00, 3));
        catalogo.put("B003", new Bike("B003","Giant Talon 29",          849.50, 8));
        catalogo.put("B004", new Bike("B004","Cannondale Trail SL 4",  1150.00, 2));
        log("Catalogo inicializado con "+catalogo.size()+" bicicletas");
    }
    public List<Bike> listarCatalogo()                     { return new ArrayList<>(catalogo.values()); }
    public Optional<Bike> buscarPorId(String id)           { return Optional.ofNullable(catalogo.get(id)); }
    public boolean hayStock(String id, int cant)           { Bike b=catalogo.get(id); return b!=null&&b.getStock()>=cant; }
    public synchronized boolean reservarStock(String id, int cant) {
        Bike b=catalogo.get(id);
        if(b==null||b.getStock()<cant){ log("STOCK INSUFICIENTE: "+id+" (req="+cant+",disp="+(b!=null?b.getStock():0)+")"); return false; }
        b.setStock(b.getStock()-cant);
        log("Reserva OK: "+id+" x"+cant+" | stock restante: "+b.getStock());
        return true;
    }
    public synchronized void liberarStock(String id, int cant) {
        Bike b=catalogo.get(id);
        if(b!=null){ b.setStock(b.getStock()+cant); log("Stock liberado: "+id+" x"+cant+" | stock: "+b.getStock()); }
    }
    public String getStatus() { return "InventoryService ACTIVO | "+catalogo.size()+" productos"; }
    private void log(String m){ System.out.printf("[%s] [InventoryBundle] %s%n",Instant.now(),m); }
}
