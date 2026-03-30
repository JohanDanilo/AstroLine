package cr.ac.una.astroline;

public class Main {
    public static void main(String[] args) {
        // Esto llama al main del App original, 
        // pero como esta clase NO es una "Application", 
        // Java no se queja de que faltan módulos.
        App.main(args);
    }
}