package com.bikestore.inventory.api;
import java.util.List;
import java.util.Optional;
/**
 * CONTRATO OSGi — paquete exportado por inventory-api bundle.
 * OrderBundle solo depende de esta interfaz, nunca de la implementacion.
 * Patrón: Service Provider + Dependency Inversion.
 */
public interface InventoryService {
    List<Bike> listarCatalogo();
    Optional<Bike> buscarPorId(String bikeId);
    boolean hayStock(String bikeId, int cantidad);
    boolean reservarStock(String bikeId, int cantidad);
    void liberarStock(String bikeId, int cantidad);
    String getStatus();
}
