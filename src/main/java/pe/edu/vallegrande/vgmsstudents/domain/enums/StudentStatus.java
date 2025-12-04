package pe.edu.vallegrande.vgmsstudents.domain.enums;

public enum StudentStatus {
    ACTIVE("ACTIVE", "Activo"),
    INACTIVE("INACTIVE", "Inactivo"),
    TRANSFERRED("TRANSFERRED", "Transferido"),
    GRADUATED("GRADUATED", "Graduado");

    private final String code;
    private final String description;

    StudentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
