package cr.ac.una.astroline.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Cliente {

    private String cedula;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String correo;
    private String fotoPath;
    private String fechaNacimiento;
    private long lastModified;
    private boolean eliminado = false;

    public Cliente() {
    }

    public Cliente(String cedula, String nombre, String apellidos, String telefono, String correo, String fotoPath, String fechaNacimiento) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
        this.fotoPath = fotoPath;
        this.fechaNacimiento = fechaNacimiento;
    }

    public boolean esMayorDe65() {
        if (fechaNacimiento == null || fechaNacimiento.isEmpty()) {
            return false;
        }
        LocalDate nacimiento = LocalDate.parse(fechaNacimiento, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDate hoy = ZonedDateTime.now(ZoneId.of("America/Costa_Rica")).toLocalDate();
        return Period.between(nacimiento, hoy).getYears() >= 65;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    @Override
    public String toString() {
        return "Cliente{cedula='" + cedula + "', nombre='" + getNombreCompleto() + "'}";
    }
}
