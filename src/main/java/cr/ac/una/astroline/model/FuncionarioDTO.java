package cr.ac.una.astroline.model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FuncionarioDTO {

    private StringProperty cedula;
    private StringProperty nombre;
    private StringProperty apellidos;
    private StringProperty telefono;
    private StringProperty correo;
    private String fotoPath;
    private ObjectProperty<LocalDate> fechaNacimiento;
    private StringProperty username;
    private StringProperty password;
    private boolean esAdmin;

    public FuncionarioDTO() {
        this.cedula = new SimpleStringProperty("");
        this.nombre = new SimpleStringProperty("");
        this.apellidos = new SimpleStringProperty("");
        this.telefono = new SimpleStringProperty("");
        this.correo = new SimpleStringProperty("");
        this.fechaNacimiento = new SimpleObjectProperty<>();
        this.username = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");

    }

    public String getCedula() {
        return cedula.get();
    }

    public void setCedula(String cedula) {
        this.cedula.set(cedula);
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getApellidos() {
        return apellidos.get();
    }

    public void setApellidos(String apellidos) {
        this.apellidos.set(apellidos);
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

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento.get();
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento.set(fechaNacimiento);
    }

    public StringProperty getCedulaProperty() {
        return cedula;
    }

    public StringProperty getNombreProperty() {
        return nombre;
    }

    public StringProperty getApellidosProperty() {
        return apellidos;
    }

    public StringProperty getTelefonoProperty() {
        return telefono;
    }

    public StringProperty getCorreoProperty() {
        return correo;
    }

    public ObjectProperty<LocalDate> getFechaNacimientoProperty() {
        return fechaNacimiento;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty getUsernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty getPasswordProperty() {
        return password;
    }

    public boolean esAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }
}
