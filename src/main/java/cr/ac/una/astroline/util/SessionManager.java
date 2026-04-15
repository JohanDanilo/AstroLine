package cr.ac.una.astroline.util;

import cr.ac.una.astroline.model.Funcionario;

/**
 * Clase para la gestión de la sesión del usuario, ya sea funcionario normal o admin
 * @author JohanDanilo
 */
// util/SessionManager.java
public class SessionManager {
    private static SessionManager instancia;
    private Funcionario funcionarioActivo;
    private String modoAcceso = "";

    private SessionManager() {}

    public static SessionManager getInstancia() {
        if (instancia == null) instancia = new SessionManager();
        return instancia;
    }

    public Funcionario getFuncionarioActivo() { return funcionarioActivo; }
    public void setFuncionarioActivo(Funcionario f) { this.funcionarioActivo = f; }
    public void cerrarSesion() { this.funcionarioActivo = null; }
    
    public String getModoAcceso() { return modoAcceso; }
    public void setModoAcceso(String modo) { this.modoAcceso = modo; } 
}
