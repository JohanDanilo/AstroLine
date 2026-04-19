package cr.ac.una.astroline.util;

import cr.ac.una.astroline.model.Ficha;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;



public class AudioManager {

    private static AudioManager INSTANCIA;
    private Map<String, Clip> sonidos = new HashMap<>();

    private AudioManager(){ cargarSonidos(); }
    
    public static AudioManager getINSTANCIA(){
        if(INSTANCIA == null)
            INSTANCIA = new AudioManager();
        return INSTANCIA;
    }
    
    private void cargarSonidos(){
        try{
            sonidos.put("estacion", cargarClip("estacion"));
            sonidos.put("ficha", cargarClip("ficha"));
            sonidos.put("llamado", cargarClip("llamado"));
            
            sonidos.put("A", cargarClip("letra_a"));
            sonidos.put("B", cargarClip("letra_b"));
            sonidos.put("C", cargarClip("letra_c"));
            sonidos.put("D", cargarClip("letra_d"));
            
            sonidos.put("0", cargarClip("num_0"));
            sonidos.put("1", cargarClip("num_1"));
            sonidos.put("2", cargarClip("num_2"));
            sonidos.put("3", cargarClip("num_3"));
            sonidos.put("4", cargarClip("num_4"));
            sonidos.put("5", cargarClip("num_5"));
            sonidos.put("6", cargarClip("num_6"));
            sonidos.put("7", cargarClip("num_7"));
            sonidos.put("8", cargarClip("num_8"));
            sonidos.put("9", cargarClip("num_9"));

            
        }
        catch(Exception e){
            System.out.println("❌ ERROR cargando: ");
            e.printStackTrace();
}
    }
    
    private Clip cargarClip(String nombreArchivo) throws IOException, UnsupportedAudioFileException, LineUnavailableException{
        
        AudioInputStream audio  = AudioSystem.getAudioInputStream(getClass()
                .getResource("/cr/ac/una/astroline/audio/"+ nombreArchivo +".wav"));
        
        Clip clip = AudioSystem.getClip();
        clip.open(audio);
        
        return clip;
    }
    
    public void reproducir(String key){
        Clip audio = sonidos.get(key);
        
        if(audio == null){
            System.out.println("[AudioManager]  Key invalida");
            return;
        }
        
        if(audio.isRunning())
            audio.stop();
        
        audio.setFramePosition(0);
        audio.start();
        
    }
    
    public void reproducirSecuencia(List<String> keys) {
        for (String key : keys) {
            Clip audio = sonidos.get(key);

            if (audio == null) {
                System.out.println("[AudioManager] Key invalida: " + key);
                return;
            }

            if (audio.isRunning()) audio.stop();
            audio.setFramePosition(0);
            audio.start();

            long duracionMs = audio.getMicrosecondLength() / 1000;
            
            try {
                Thread.sleep(duracionMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    
    //Metodo Exclusivo de poyeccion
    public void llamarFicha(Ficha ficha) {

        List<String> secuencia = new java.util.ArrayList<>();
        secuencia.add("ficha");
        
        secuencia.add(ficha.getId());
        
        String numero = String.valueOf(ficha.getNumeroFormateado());
        for (char digito : numero.toCharArray()) 
            secuencia.add(String.valueOf(digito));

        secuencia.add("llamado");
  
        secuencia.add("estacion");

        secuencia.add(" ");

        new Thread(() -> reproducirSecuencia(secuencia)).start();
    }
    
}
