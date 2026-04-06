/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.astroline.model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
/**
 *
 * @author takka_sama
 */
public class EstacionDTO {
    private StringProperty id;
    private StringProperty nombre;
    private StringProperty sucursalId;
    private BooleanProperty preferencial;
    private BooleanProperty estaActiva;

    public EstacionDTO() {
        this.id = new SimpleStringProperty("");
        this.nombre = new SimpleStringProperty("");
        this.sucursalId = new SimpleStringProperty("");
        this.preferencial = new SimpleBooleanProperty(false);
        this.estaActiva = new SimpleBooleanProperty(false);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getSucursalId() {
        return sucursalId.get();
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId.set(sucursalId);
    }

    public boolean isPreferencial() {
        return preferencial.get();
    }

    public void setPreferencial(boolean preferencial) {
        this.preferencial.set(preferencial);
    }

    public boolean isEstaActiva() {
        return estaActiva.get();
    }

    public void setEstaActiva(boolean estaActiva) {
        this.estaActiva.set(estaActiva);
    }
    

    public StringProperty getIdProperty() {
        return id;
    }

    public StringProperty getNombreProperty() {
        return nombre;
    }

    public StringProperty getSucursalIdProperty() {
        return sucursalId;
    }

    public BooleanProperty getPreferencialProperty() {
        return preferencial;
    }

    public BooleanProperty getEstaActivaProperty() {
        return estaActiva;
    }
    
    
    
}
