package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EstacionDTO {

    private StringProperty id;
    private StringProperty nombre;
    private StringProperty sucursalId;
    private BooleanProperty preferencial;
    private BooleanProperty estaActiva;
    private List<String> tramiteIds;

    public EstacionDTO() {
        this.id = new SimpleStringProperty("");
        this.nombre = new SimpleStringProperty("");
        this.sucursalId = new SimpleStringProperty("");
        this.preferencial = new SimpleBooleanProperty(false);
        this.estaActiva = new SimpleBooleanProperty(true);
        this.tramiteIds = new ArrayList<>();
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

    public List<String> getTramiteIds() {
        return tramiteIds;
    }

    public void setTramiteIds(List<String> tramiteIds) {
        this.tramiteIds = tramiteIds != null ? tramiteIds : new ArrayList<>();
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

    public BooleanProperty getPrefencialProperty() {
        return preferencial;
    }

    public BooleanProperty getEstaActivaProperty() {
        return estaActiva;
    }
}
