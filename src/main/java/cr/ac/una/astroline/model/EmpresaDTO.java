package cr.ac.una.astroline.model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmpresaDTO {

    private StringProperty nombre;
    private StringProperty logoPath;
    private StringProperty pinAdmin;
    private StringProperty telefono;
    private StringProperty correo;
    private StringProperty direccion;

    public EmpresaDTO() {
        this.nombre = new SimpleStringProperty("");
        this.telefono = new SimpleStringProperty("");
        this.correo = new SimpleStringProperty("");
        this.logoPath = new SimpleStringProperty("");
        this.pinAdmin = new SimpleStringProperty("");
        this.direccion = new SimpleStringProperty("");
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getTelefono() {
        return telefono.get();
    }

    public void setTelefono(String telefono) {
        this.telefono.set(telefono);
    }

    public String getCorreo() {
        return correo.get();
    }

    public void setCorreo(String correo) {
        this.correo.set(correo);
    }

    public String getLogoPath() {
        return logoPath.get();
    }

    public void setLogoPath(String logoPath) {
        this.logoPath.set(logoPath);
    }

    public String getPinAdmin() {
        return pinAdmin.get();
    }

    public void setPinAdmin(String pin) {
        this.pinAdmin.set(pin);
    }

    public String getDireccion() {
        return direccion.get();
    }

    public void setDireccion(String direccion) {
        this.direccion.set(direccion);
    }

    public StringProperty getNombreProperty() {
        return nombre;
    }

    public StringProperty getTelefonoProperty() {
        return telefono;
    }

    public StringProperty getCorreoProperty() {
        return correo;
    }

    public StringProperty logoPathProperty() {
        return logoPath;
    }

    public StringProperty pinAdminProperty() {
        return pinAdmin;
    }

    public StringProperty direccionProperty() {
        return direccion;
    }

}
