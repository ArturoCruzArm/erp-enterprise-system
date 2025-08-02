package com.erp.system.inventory.enums;

public enum UnitOfMeasure {
    PIECE("pcs", "Pieza"),
    KILOGRAM("kg", "Kilogramo"),
    GRAM("g", "Gramo"),
    LITER("L", "Litro"),
    MILLILITER("mL", "Mililitro"),
    METER("m", "Metro"),
    CENTIMETER("cm", "Centímetro"),
    SQUARE_METER("m²", "Metro cuadrado"),
    BOX("box", "Caja"),
    DOZEN("dz", "Docena"),
    PACK("pack", "Paquete"),
    BOTTLE("bottle", "Botella"),
    BAG("bag", "Bolsa");
    
    private final String code;
    private final String description;
    
    UnitOfMeasure(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
}