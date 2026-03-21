package cr.ac.una.astroline.model;

/**
 * Representa la información general de la empresa.
 * Se persiste en data/empresa.json
 *
 * @author JohanDanilo
 */
public class Empresa {

    private String nombre;
    private String logoPath;
    private String pinAdmin;
    private String telefono;
    private String correo;
    private String direccion;

    public Empresa() {
    }

    public Empresa(String nombre, String logoPath, String pinAdmin,
            String telefono, String correo, String direccion) {
        this.nombre = nombre;
        this.logoPath = logoPath;
        this.pinAdmin = pinAdmin;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getPinAdmin() { return pinAdmin; }
    public void setPinAdmin(String pinAdmin) { this.pinAdmin = pinAdmin; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() {
        return "Empresa{nombre='" + nombre + "'}";
    }
}