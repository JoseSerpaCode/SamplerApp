package interfaceSampler;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class BackgroundMusicPlayer {

    private Clip musicClip;
    private long clipTimePosition = 0; // Para pausar y reanudar
    private JProgressBar barraDeProgresoCancion;
    private JLabel label_MusicTimeInicial, label_MusicTimeFinal, label_TituloCancion;
    private JButton playPauseButton; // Botón para alternar play/pause

    public BackgroundMusicPlayer(JProgressBar barra, JLabel tiempoInicial, JLabel tiempoFinal, JLabel titulo) {
        this.barraDeProgresoCancion = barra;
        this.label_MusicTimeInicial = tiempoInicial;
        this.label_MusicTimeFinal = tiempoFinal;
        this.label_TituloCancion = titulo;
    }

    // Método para configurar el botón de play/pause
    public void setPlayPauseButton(JButton button) {
        this.playPauseButton = button;
    }

    // Método para subir canción usando FileDialog
    public void subirCancion() {
        FileDialog fileDialog = new FileDialog((Frame) null, "Seleccionar archivo de audio", FileDialog.LOAD);
        fileDialog.setFile("*.mp3;*.wav"); // Filtro para archivos MP3 y WAV
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            File selectedFile = new File(directory, filename);
            label_TituloCancion.setText(selectedFile.getName().replaceFirst("[.][^.]+$", "")); // Quitar extensión
            loadMusic(selectedFile); // Llama a tu método para cargar la música
        }
    }

    // Cargar la canción
    private void loadMusic(File musicFile) {
        try {
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);

            // Calcular duración
            long durationInSeconds = musicClip.getMicrosecondLength() / 1_000_000;
            label_MusicTimeFinal.setText(formatTime(durationInSeconds));

            // Configurar la barra de progreso
            barraDeProgresoCancion.setMaximum((int) durationInSeconds);
            barraDeProgresoCancion.setValue(0);

            // Reiniciar el icono de play
            if (playPauseButton != null) {
                playPauseButton.setIcon(new ImageIcon(getClass().getResource("/img/Play.png")));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar el archivo de audio: " + e.getMessage());
        }
    }

    // Método para pausar música
    public void pauseMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            clipTimePosition = musicClip.getMicrosecondPosition(); // Guardar posición actual
            musicClip.stop(); // Detener el clip
        }
    }

// Método para reproducir música
    public void playMusic() {
        if (musicClip != null) {
            // Si ya se está reproduciendo, no hacer nada
            if (musicClip.isRunning()) {
                return;
            }

            // Retomar desde la última posición
            musicClip.setMicrosecondPosition(clipTimePosition);
            musicClip.start(); // Iniciar reproducción

            // Hilo para actualizar la barra de progreso de manera fluida
            new Thread(() -> {
                while (musicClip != null && musicClip.isRunning()) {
                    // Actualizar la barra de progreso
                    int progress = (int) (musicClip.getMicrosecondPosition() / 1_000_000); // Convertir microsegundos a segundos
                    barraDeProgresoCancion.setValue(progress); // Actualizar barra de progreso
                    label_MusicTimeInicial.setText(formatTime(progress)); // Mostrar tiempo inicial

                    try {
                        Thread.sleep(50); // Actualiza cada 50ms para mayor fluidez
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    // Reiniciar música
    public void reiniciarCancion() {
        if (musicClip != null) {
            musicClip.stop(); // Detener la reproducción actual
            musicClip.setFramePosition(0); // Reiniciar el clip a la posición inicial
            clipTimePosition = 0; // Reiniciar el contador del tiempo
            barraDeProgresoCancion.setValue(0); // Reiniciar la barra de progreso
            label_MusicTimeInicial.setText("0:00"); // Reiniciar el texto del tiempo inicial

            // Cambiar icono a "pause" después de reiniciar
            if (playPauseButton != null) {
                playPauseButton.setIcon(new ImageIcon(getClass().getResource("/img/Pause.png")));
            }
        }
    }

    public long getMusicLength() {
        return musicClip != null ? musicClip.getMicrosecondLength() : 0;
    }

    public void setMusicPosition(long seconds) {
        if (musicClip != null) {
            long microseconds = seconds * 1_000_000; // Convertir segundos a microsegundos
            if (microseconds <= musicClip.getMicrosecondLength()) {
                musicClip.setMicrosecondPosition(microseconds);
                clipTimePosition = microseconds; // Actualizar también la posición guardada
            }
        }
    }
    
    public void adjustSongVolume(float volumePercentage) {
    if (musicClip != null && musicClip.isOpen()) {
        try {
            FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);

            // Convierte el porcentaje en un rango de decibelios
            float minGain = gainControl.getMinimum();
            float maxGain = gainControl.getMaximum();
            float newGain = minGain + (volumePercentage / 100.0f) * (maxGain - minGain);

            gainControl.setValue(newGain);
        } catch (IllegalArgumentException e) {
            System.err.println("Control de volumen no soportado: " + e.getMessage());
        }
    }
}

    // Formatear tiempo en minutos:segundos
    private String formatTime(long seconds) {
        return String.format("%d:%02d", TimeUnit.SECONDS.toMinutes(seconds), seconds % 60);
    }
}
