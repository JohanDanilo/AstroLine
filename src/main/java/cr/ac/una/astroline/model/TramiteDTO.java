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
public class TramiteDTO {
    private StringProperty id;
    private StringProperty nombre;
    private StringProperty descripcion;
    private BooleanProperty activo;

    public TramiteDTO() {
        this.id = new SimpleStringProperty("");
        this.nombre = new SimpleStringProperty("");
        this.descripcion = new SimpleStringProperty("");
        this.activo = new SimpleBooleanProperty();
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

    public String getDescripcion() {
        return descripcion.get();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    public boolean isActivo() {
        return activo.get();
    }

    public void setActivo(boolean activo) {
        this.activo.get();
    }
    

    public StringProperty getIdProperty() {
        return id;
    }

    public StringProperty getNombreProperty() {
        return nombre;
    }

    public StringProperty getDescripcionProperty() {
        return descripcion;
    }

    public BooleanProperty getActivoProperty() {
        return activo;
    }
    
    
    
}
