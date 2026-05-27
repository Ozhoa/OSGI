package com.bikestore.inventory.api;
public class Bike {
    private final String id, modelo;
    private final double precio;
    private int stock;
    public Bike(String id, String modelo, double precio, int stock) {
        this.id=id; this.modelo=modelo; this.precio=precio; this.stock=stock;
    }
    public String getId()      { return id; }
    public String getModelo()  { return modelo; }
    public double getPrecio()  { return precio; }
    public int getStock()      { return stock; }
    public void setStock(int s){ this.stock=s; }
    public String toString()   { return String.format("Bike{id='%s', modelo='%s', precio=%.2f, stock=%d}",id,modelo,precio,stock); }
}
