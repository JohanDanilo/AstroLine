
package cr.ac.una.astroline.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioService {

    private static AudioService instancia;

    private AudioService() {
        iniciarWorker();
    }

    public static AudioService getInstancia() {
        if (instancia == null) {
            instancia = new AudioService();
        }
        return instancia;
    }

    private final BlockingQueue<String> cola = new LinkedBlockingQueue<>(10);

    private volatile boolean habilitado = true;

    private final SistemaOperativo so = detectarSO();

    public void anunciarFicha(String letra, String numeroFormateado, String nombreEstacion) {
        if (!habilitado) return;

        String texto = "Ficha " + letra + " " + separarDigitos(numeroFormateado)
                     + ", favor pasar a la " + nombreEstacion;

        encolar(texto);
    }

    public void anunciar(String texto) {
        if (!habilitado || texto == null || texto.isBlank()) return;
        encolar(texto);
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
        if (!habilitado) cola.clear(); 
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    private void iniciarWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    String texto = cola.take(); 
                    if (habilitado) reproducir(texto);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "astroline-audio-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void encolar(String texto) {

        boolean encolado = cola.offer(texto);
        if (!encolado) {
            System.out.println("[AudioService] Cola llena, anuncio descartado: " + texto);
        }
    }

    private void reproducir(String texto) {
        try {
            String[] comando = construirComando(texto);
            if (comando == null) {
                System.err.println("[AudioService] SO no soportado para TTS: " + so);
                return;
            }

            Process proceso = new ProcessBuilder(comando).redirectErrorStream(true).start();
            proceso.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());

            int exitCode = proceso.waitFor();
            if (exitCode != 0) {
                System.err.println("[AudioService] TTS terminó con código: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("[AudioService] Error reproduciendo audio: " + e.getMessage());
        }
    }

    private String[] construirComando(String texto) {
        String textoSeguro = texto.replace("'", " ").replace("\"", " ").replace("`", " ");

        return switch (so) {
            case WINDOWS -> new String[]{
                "powershell", "-NoProfile", "-NonInteractive", "-Command",
                "Add-Type -AssemblyName System.Speech; " +
                "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "$s.SelectVoiceByHints('es'); " + 
                "$s.Speak('" + textoSeguro + "');"
            };
            case MACOS -> new String[]{
                "say", "-v", "Monica", 
                textoSeguro
            };
            case LINUX -> new String[]{ "espeak-ng", "-v", "es","-s", "140", textoSeguro};
            default -> null;
        };
    }

    private String separarDigitos(String numero) {
        if (numero == null || numero.isBlank()) return "";
        return String.join(" ", numero.split(""));
    }

    private enum SistemaOperativo { WINDOWS, MACOS, LINUX, DESCONOCIDO }

    private SistemaOperativo detectarSO() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win"))   return SistemaOperativo.WINDOWS;
        if (os.contains("mac"))   return SistemaOperativo.MACOS;
        if (os.contains("nix") || os.contains("nux")) return SistemaOperativo.LINUX;
        return SistemaOperativo.DESCONOCIDO;
    }
}