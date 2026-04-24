package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.EmpresaDTO;
import cr.ac.una.astroline.util.GsonUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class EmpresaService{

    private Empresa empresa;

    private final ObjectProperty<Empresa> empresaProperty = new SimpleObjectProperty<>();

    private static EmpresaService instancia;

    private static final String ARCHIVO_JSON = "empresa.json";

    private EmpresaService() {
        cargarEmpresa();
    }

    public static EmpresaService getInstancia() {
        if (instancia == null) {
            instancia = new EmpresaService();
        }
        return instancia;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public ObjectProperty<Empresa> getEmpresaProperty() {
        return empresaProperty;
    }

    public void cargarEnDTO(Empresa empresa, EmpresaDTO dto) {
        if (empresa == null || dto == null) {
            return;
        }

        dto.setNombre(empresa.getNombre());
        dto.setTelefono(empresa.getTelefono());
        dto.setCorreo(empresa.getCorreo());
        dto.setLogoPath(empresa.getLogoPath());
        dto.setDireccion(empresa.getDireccion());
        dto.setPinAdmin(empresa.getPinAdmin());
    }

    public Empresa dtoAEmpresa(EmpresaDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Empresa(dto.getNombre(), dto.getLogoPath(), dto.getPinAdmin(), dto.getTelefono(), dto.getCorreo(), dto.getDireccion());
    }

    public boolean actualizar(Empresa empresaActualizada) {
        if (empresaActualizada == null) {
            return false;
        }
        empresaActualizada.setLastModified(System.currentTimeMillis());
        this.empresa = empresaActualizada;
        empresaProperty.set(this.empresa);
        GsonUtil.guardar(this.empresa, ARCHIVO_JSON);
        return true;
    }

    private void cargarEmpresa() {
        Empresa cargada = GsonUtil.leer(ARCHIVO_JSON, Empresa.class);

        this.empresa = cargada != null ? cargada : new Empresa();
        empresaProperty.set(this.empresa);
    }

}
