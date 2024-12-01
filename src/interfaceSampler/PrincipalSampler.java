package interfaceSampler;

import javax.swing.SwingUtilities;

import com.fazecast.jSerialComm.SerialPort;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.*;
import java.util.Scanner;
import javax.sound.sampled.*;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.Timer;

/**
 * @author José_Serpa
 */
public class PrincipalSampler extends javax.swing.JFrame {

    private boolean[] noteIsOn = new boolean[6];
    private final File[] audioFiles; // Array para almacenar el archivo de audio de cada pad
    private final Clip[] audioClips; // Array para controlar la reproducción de cada pad
    private final JSlider[] volumeSliders; // Array de sliders de volumen
    private PanelRound[] padPanels;
    private static final int NUMBER_OF_PADS = 6; // Número de pads
    private BackgroundMusicPlayer musicPlayer; // Declarar como atributo de clase
    private SerialPort puerto;
    private Thread lecturaHilo;

    public PrincipalSampler() {
        initComponents();

        noteIsOn = new boolean[6];  // Suponiendo que tienes 6 pads

        ImageIcon icon = new ImageIcon(getClass().getResource("/img/padland.jpg"));
        setIconImage(icon.getImage());

        // Inicializa los arrays
        audioFiles = new File[NUMBER_OF_PADS];
        audioClips = new Clip[NUMBER_OF_PADS];
        volumeSliders = new JSlider[NUMBER_OF_PADS]; // Inicialización de los sliders

        // Cargar los sonidos guardados al iniciar el programa
        loadAudioFiles();

        // Configuración para asignar sonido a los pads
        JLabel[] asignarSonido = {label_SeleccionarSonido1, label_SeleccionarSonido2, label_SeleccionarSonido3, label_SeleccionarSonido4, label_SeleccionarSonido5, label_SeleccionarSonido6};

        PanelRound[] panelAsignar = {botonSelecccionarSonido_Pad1, botonSelecccionarSonido_Pad2, botonSelecccionarSonido_Pad3, botonSelecccionarSonido_Pad4, botonSelecccionarSonido_Pad5, botonSelecccionarSonido_Pad6};

        for (int i = 0; i < asignarSonido.length; i++) {
            final int index = i; // Usa el índice directamente para evitar confusión
            asignarSonido[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    selectAudioFile(index);
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    panelAsignar[index].setBackground(panelAsignar[index].getBackground().darker());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    panelAsignar[index].setBackground(panelAsignar[index].getBackground().brighter());
                }
            });
        }

        //Configuración para Consultar sonido a los pads
        JLabel[] consultarSonido = {label_ConsultarSonido1, label_ConsultarSonido2, label_ConsultarSonido3, label_ConsultarSonido4, label_ConsultarSonido5, label_ConsultarSonido6};

        PanelRound[] panelConsultar = {botonConsultarSonido_Pad1, botonConsultarSonido_Pad2, botonConsultarSonido_Pad3, botonConsultarSonido_Pad44, botonConsultarSonido_Pad5, botonConsultarSonido_Pad6};

        for (int i = 0; i < consultarSonido.length; i++) {
            final int index = i; // Usa el índice directamente para evitar confusión
            consultarSonido[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showAudioFileInfo(index);
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    panelConsultar[index].setBackground(panelConsultar[index].getBackground().darker());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    panelConsultar[index].setBackground(panelConsultar[index].getBackground().brighter());
                }
            });
        }

        //Configuración para Eliminar sonido a los pads
        JLabel[] eliminarSonido = {label_EliminarSonido1, label_EliminarSonido2, label_EliminarSonido3, label_EliminarSonido4, label_EliminarSonido5, label_EliminarSonido6};

        PanelRound[] panelEliminar = {botonEliminarSonido_Pad1, botonEliminarSonido_Pad2, botonEliminarSonido_Pad3, botonEliminarSonido_Pad4, botonEliminarSonido_Pad5, botonEliminarSonido_Pad66};

        for (int i = 0; i < eliminarSonido.length; i++) {
            final int index = i; // Usa el índice directamente para evitar confusión
            eliminarSonido[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    removeAudioFile(index);
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    panelEliminar[index].setBackground(panelEliminar[index].getBackground().darker());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    panelEliminar[index].setBackground(panelEliminar[index].getBackground().brighter());
                }
            });
        }

        // Array para los paneles de los pads
        padPanels = new PanelRound[]{padSound1, padSound2, padSound3, padSound4, padSound5, padSound6};

        // Array para los labels de los pads
        JLabel[] labels = {labelPadSound1, labelPadSound2, labelPadSound3, labelPadSound4, labelPadSound5, labelPadSound6};

        // Configuración para los pads de reproducción
        for (int i = 0; i < labels.length; i++) {
            final int padIndex = i;

            labels[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    padPanels[padIndex].setBackground(padPanels[padIndex].getBackground().darker()); // Cambia a un color más oscuro al presionar
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    padPanels[padIndex].setBackground(padPanels[padIndex].getBackground().brighter()); // Regresa al color original al soltar
                    try {
                        playAudio(padIndex); // Reproduce el sonido asociado al pad
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(PrincipalSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
            });
        }

        // Inicializa los sliders de volumen
        volumeSliders[0] = ControlVolumen1;
        volumeSliders[1] = ControlVolumen2;
        volumeSliders[2] = ControlVolumen3;
        volumeSliders[3] = ControlVolumen4;
        volumeSliders[4] = ControlVolumen5;
        volumeSliders[5] = ControlVolumen6;

        for (int i = 0; i < NUMBER_OF_PADS; i++) {
            final int padIndex = i; // Usa el índice específico para cada slider y cada pad

            // Aplicar la apariencia personalizada al slider actual
            volumeSliders[i].setUI(new CustomSliderUI(volumeSliders[i]));

            volumeSliders[i].setMinorTickSpacing(5);
            volumeSliders[i].setMajorTickSpacing(20);
            volumeSliders[i].setPaintTicks(true);
            volumeSliders[i].setValue(100); // Volumen al 100% inicialmente

            volumeSliders[i].addChangeListener((ChangeEvent evt) -> {
                adjustVolume(evt, padIndex); // Pasamos el índice del pad correspondiente
            });
        }

        //Configuración para Controles de Cancion
        JLabel[] controlesCancion = {consultarInfo, reiniciarCancion, pauseCancion, subirCancion, playCancion};

        PanelRound[] panelControlesCancion = {panelConsultarInfo, panelReiniciarCancion, panelPauseCancion, panelSubirCancion, panelPlayCancion};

        for (int i = 0; i < controlesCancion.length; i++) {
            final int index = i; // Usa el índice directamente para evitar confusión
            controlesCancion[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    panelControlesCancion[index].setBackground(panelControlesCancion[index].getBackground().darker());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    panelControlesCancion[index].setBackground(panelControlesCancion[index].getBackground().brighter());
                }
            });
        }

        musicPlayer = new BackgroundMusicPlayer(
                barraDeProgresoCancion,
                label_MusicTimeInicial,
                label_MusicTimeFinal,
                label_TituloCancion
        );

        //Control de Volumen de la Cancion
        sliderVolumenCancion.setMinorTickSpacing(10);
        sliderVolumenCancion.setMajorTickSpacing(20);
        sliderVolumenCancion.setPaintTicks(true);
        sliderVolumenCancion.setValue(100); // Volumen inicial al 100%

        sliderVolumenCancion.addChangeListener(evt -> {
            int volumeValue = sliderVolumenCancion.getValue(); // Obtiene el valor del slider
            musicPlayer.adjustSongVolume(volumeValue);        // Ajusta el volumen de la canción
        });

        // Llama a la función para buscar puertos COM
        detectarPuertosCOM();
    }

    // Método para ajustar el volumen de un pad específico cuando el slider cambia
    private void adjustVolume(ChangeEvent evt, int padIndex) {
        JSlider slider = (JSlider) evt.getSource();
        float volume = slider.getValue() / 100.0f; // Convertir el valor del slider a un rango de 0.0 a 1.0
        setClipVolume(padIndex, volume); // Ajustar el volumen del clip correspondiente al pad
    }

    // Método para establecer el volumen del clip de audio
    private void setClipVolume(int padNumber, float volume) {
        if (audioClips[padNumber] != null) {
            try {
                // Ajustar el volumen manualmente usando la ganancia
                FloatControl volumeControl = (FloatControl) audioClips[padNumber].getControl(FloatControl.Type.MASTER_GAIN);

                // Si el volumen es 0, establecerlo en -80 dB (que sería silencio)
                if (volume == 0) {
                    volumeControl.setValue(-80.0f); // Silence
                } else {
                    // Si el volumen es mayor que 0, usamos una escala logarítmica para que sea más natural
                    volumeControl.setValue(20f * (float) Math.log10(volume)); // Escala logarítmica para el volumen
                }
            } catch (IllegalArgumentException e) {
                System.out.println("No se pudo establecer el control de volumen para el clip.");
            }
        }
    }

    // Método para guardar la configuración de los pads
    private void saveAudioFiles() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("audioFiles.txt"))) {
            for (File audioFile : audioFiles) {
                if (audioFile != null) {
                    writer.write(audioFile.getAbsolutePath());
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Sonidos guardados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar los sonidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método que carga los sonidos guardados al iniciar el programa
    private void loadAudioFiles() {
        try (BufferedReader reader = new BufferedReader(new FileReader("audioFiles.txt"))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null && i < audioFiles.length) {
                File audioFile = new File(line);
                if (audioFile.exists()) {
                    audioFiles[i] = audioFile;
                }
                i++;
            }
        } catch (IOException e) {
        }
    }

    // Método para reestablecer todos los pads
    private void resetAudioFiles() {

        // Confirma la eliminación con el usuario
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar todos los sonidos asignados a los pads?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        // Si el usuario confirma, elimina el sonido
        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = 0; i < audioFiles.length; i++) {
                audioFiles[i] = null; // Reinicia los archivos de audio
            }
            JOptionPane.showMessageDialog(this, "Sonidos eliminados correctamente.", "Eliminar Sonido", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método para seleccionar el audio de un pad usando FileDialog
    private void selectAudioFile(int padNumber) {
        FileDialog fileDialog = new FileDialog((Frame) null, "Seleccionar archivo de audio", FileDialog.LOAD);
        fileDialog.setFile("*.mp3;*.wav"); // Filtro para archivos MP3 y WAV
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            audioFiles[padNumber] = new File(directory, filename);
            System.out.println("Archivo asignado al pad " + (padNumber + 1) + ": " + audioFiles[padNumber].getName());
        }
    }

    // Método para reproducir el sonido de un pad específico
    private void playAudio(int padNumber) throws IOException {
        File audioFile = audioFiles[padNumber];
        if (audioFile == null) {
            System.out.println("No se ha asignado un sonido al pad: " + (padNumber + 1));
            return;
        }

        try {
            // Detenemos la reproducción anterior, si la hay
            if (audioClips[padNumber] != null && audioClips[padNumber].isRunning()) {
                audioClips[padNumber].stop();
            }

            // Cargamos y reproducimos el nuevo archivo
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            audioClips[padNumber] = AudioSystem.getClip();
            audioClips[padNumber].open(audioStream);

            // Establecer el volumen en el momento de la reproducción
            setClipVolume(padNumber, volumeSliders[padNumber].getValue() / 100.0f); // Usar el slider correspondiente al pad actual

            audioClips[padNumber].start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            JOptionPane.showMessageDialog(this, "Error al reproducir el audio.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAudioFileInfo(int padNumber) {
        // Obtén el archivo asignado al pad correspondiente
        File audioFile = audioFiles[padNumber];

        // Si no hay ningún archivo asignado, muestra un mensaje de advertencia
        if (audioFile == null) {
            JOptionPane.showMessageDialog(this, "No hay pista seleccionada.", "Información de Pista", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Extrae el nombre del archivo y su formato
        String fileName = audioFile.getName();
        String format = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();

        // Verifica si el formato del archivo es compatible (solo MP3 y WAV permitidos)
        if (!format.equals("MP3") && !format.equals("WAV")) {
            JOptionPane.showMessageDialog(this, "Formato de archivo no compatible.", "Información de Pista", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Intenta obtener la duración del archivo de audio
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile); // Carga el archivo de audio
            AudioFormat audioFormat = audioStream.getFormat(); // Obtiene el formato del audio
            long frames = audioStream.getFrameLength(); // Obtiene el número de frames en el archivo
            double durationInSeconds = (frames + 0.0) / audioFormat.getFrameRate(); // Calcula la duración en segundos

            // Muestra la información del archivo en un cuadro de diálogo
            JOptionPane.showMessageDialog(this,
                    "Nombre: " + fileName + "\nDuración: " + String.format("%.2f", durationInSeconds) + " segundos\nFormato: " + format,
                    "Información de Pista",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException | IOException | UnsupportedAudioFileException e) {
// Si ocurre un error al obtener la información, muestra un mensaje de error
            JOptionPane.showMessageDialog(this, "Error al obtener la duración de la pista.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para eliminar el archivo de audio de un pad específico
    private void removeAudioFile(int padNumber) {
        // Verifica si hay un archivo asignado al pad
        if (audioFiles[padNumber] == null) {
            JOptionPane.showMessageDialog(this, "No hay un sonido asignado a este pad.", "Eliminar Sonido", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Confirma la eliminación con el usuario
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar el sonido asignado al pad " + (padNumber + 1) + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        // Si el usuario confirma, elimina el sonido
        if (confirm == JOptionPane.YES_OPTION) {
            audioFiles[padNumber] = null; // Elimina el archivo de audio asignado
            volumeSliders[padNumber].setValue(100); // Restablece el volumen al 100%

            JOptionPane.showMessageDialog(this, "Sonido eliminado correctamente del pad " + (padNumber + 1) + ".", "Eliminar Sonido", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void detectarPuertosCOM() {
        // Obtiene los puertos COM disponibles
        SerialPort[] puertos = SerialPort.getCommPorts();

        // Limpia el ComboBox (en caso de que lo llames varias veces)
        comboBoxCOM.removeAllItems();

        // Añade los nombres de los puertos al ComboBox
        for (SerialPort puerto : puertos) {
            comboBoxCOM.addItem(puerto.getSystemPortName()); // Ejemplo: "COM3"
        }

        // Si no hay puertos disponibles, muestra un mensaje
        if (comboBoxCOM.getItemCount() == 0) {
            comboBoxCOM.addItem("No hay puertos disponibles");
        }
    }

    private void conectarPuerto() {
        String puertoSeleccionado = (String) comboBoxCOM.getSelectedItem();

        if (puertoSeleccionado == null || puertoSeleccionado.equals("No hay puertos disponibles")) {
            JOptionPane.showMessageDialog(this, "Selecciona un puerto válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (puerto != null && puerto.isOpen()) {
            JOptionPane.showMessageDialog(this, "El puerto ya está conectado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        puerto = SerialPort.getCommPort(puertoSeleccionado);
        puerto.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (puerto.openPort()) {
            JOptionPane.showMessageDialog(this, "Conexión exitosa con " + puertoSeleccionado, "Conectado", JOptionPane.INFORMATION_MESSAGE);
            actualizarIndicadorEstado(true);

            // Iniciar lectura de datos
            iniciarLecturaDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo conectar al puerto " + puertoSeleccionado, "Error", JOptionPane.ERROR_MESSAGE);
            actualizarIndicadorEstado(false);
        }
    }

    private void desconectarPuerto(SerialPort puerto) {
        if (puerto != null && puerto.isOpen()) {
            puerto.closePort();
            JOptionPane.showMessageDialog(this, "Puerto desconectado.", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
            actualizarIndicadorEstado(false);

            // Detener hilo de lectura
            if (lecturaHilo != null && lecturaHilo.isAlive()) {
                lecturaHilo.interrupt();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No hay un puerto conectado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarIndicadorEstado(boolean conectado) {
        if (conectado) {
            labelEstado.setText("Estado: Conectado");
            labelEstado.setForeground(Color.GREEN);
        } else {
            labelEstado.setText("Estado: Desconectado");
            labelEstado.setForeground(Color.RED);
        }
    }

    private void iniciarLecturaDatos() {
        lecturaHilo = new Thread(() -> {
            try (Scanner serialScanner = new Scanner(puerto.getInputStream())) {
                while (!Thread.currentThread().isInterrupted() && serialScanner.hasNextLine()) {
                    String linea = serialScanner.nextLine();
                    System.out.println("Mensaje recibido: " + linea);

                    try {
                        // Verifica si el mensaje contiene "Note On"
                        if (linea.contains("Note On")) {
                            String[] partes = linea.split(" "); // Dividimos por espacios
                            int nota = Integer.parseInt(partes[5]); // Extraer la nota
                            int velocidad = Integer.parseInt(partes[8]); // Extraer la velocidad

                            // Procesar la nota si la velocidad es mayor a 5
                            if (velocidad > 5) {
                                procesarNotaMidi(nota, velocidad);
                            }
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        System.err.println("Error al procesar mensaje MIDI: " + ex.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        lecturaHilo.start();
    }

    private void procesarNotaMidi(int nota, int velocidad) {
        // Verificar que la velocidad sea mayor a 5 para activar el pad
        if (velocidad > 5) {
            // Mapeo de las notas a los pads: 36 -> Pad 1, 38 -> Pad 2, etc.
            switch (nota) {
                case 36:
                    activarPad(0); // Pad 1
                    break;
                case 38:
                    activarPad(1); // Pad 2
                    break;
                case 40:
                    activarPad(2); // Pad 3
                    break;
                case 42:
                    activarPad(3); // Pad 4
                    break;
                case 44:
                    activarPad(4); // Pad 5
                    break;
                case 46:
                    activarPad(5); // Pad 6
                    break;
                default:
                    break; // Si no es ninguna de las notas relevantes, no hacemos nada
            }
        }
    }

    // Método para activar un pad (cambiar su color y reproducir el sonido)
    private void activarPad(int padIndex) {
        // Comprobamos si el color ya está oscuro para evitar oscurecerlo varias veces
        if (noteIsOn[padIndex]) {
            return;  // Si la nota ya está activada, no hacemos nada más
        }

        // Cambiar color del panel del pad (más oscuro temporalmente)
        Color originalColor = padPanels[padIndex].getBackground();
        padPanels[padIndex].setBackground(originalColor.darker());  // Cambia el color a más oscuro

        // Reproducir el sonido asociado al pad
        try {
            playAudio(padIndex); // Reproduce el sonido correspondiente
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PrincipalSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Marca que el pad está activado y restauramos el color solo después de un pequeño retardo
        noteIsOn[padIndex] = true;

        // Restaurar al color original inmediatamente después de un pequeño retardo
        new Thread(() -> {
            try {
                Thread.sleep(200);  // Tiempo de retardo, puedes ajustarlo según necesites
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                // Restaurar el color original del panel del pad
                padPanels[padIndex].setBackground(originalColor);
                noteIsOn[padIndex] = false;  // Marca que el pad ha dejado de estar activado
            });
        }).start();
    }

// Método para desactivar un pad (restaurar su color a su estado original)
    private void desactivarPad(int padIndex) {
        // Cambiar el color del panel de vuelta al original (cuando ya no está activado)
        Color originalColor = padPanels[padIndex].getBackground().brighter();
        padPanels[padIndex].setBackground(originalColor);
        noteIsOn[padIndex] = false;  // Marca el pad como desactivado
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JPanel();
        panelPrincipal_Uno = new interfaceSampler.PanelRound();
        panelPerfil = new interfaceSampler.PanelRound();
        label_PerfilSeleccionado = new javax.swing.JLabel();
        label_ImageLibrary = new javax.swing.JLabel();
        panelGuardar = new interfaceSampler.PanelRound();
        botonGuardar = new interfaceSampler.PanelRound();
        label_Guardar = new javax.swing.JLabel();
        label_TituloGuardar = new javax.swing.JLabel();
        panelTextoGuardar = new interfaceSampler.PanelRound();
        label_TextoGuardar = new javax.swing.JLabel();
        panelReestablecer = new interfaceSampler.PanelRound();
        botonReestablecer = new interfaceSampler.PanelRound();
        label_Reestablecer = new javax.swing.JLabel();
        label_TituloReestablecer = new javax.swing.JLabel();
        panelTextoReestablecer = new interfaceSampler.PanelRound();
        label_TextoReestablecer = new javax.swing.JLabel();
        panelPrincipal_Dos = new interfaceSampler.PanelRound();
        panelHeaderPads = new interfaceSampler.PanelRound();
        labelTitulo_PADS = new javax.swing.JLabel();
        panelConsultarInfo = new interfaceSampler.PanelRound();
        consultarInfo = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        labelPerfil_Seleccionado = new javax.swing.JLabel();
        padSound1 = new interfaceSampler.PanelRound();
        labelPadSound1 = new javax.swing.JLabel();
        padSound2 = new interfaceSampler.PanelRound();
        labelPadSound2 = new javax.swing.JLabel();
        padSound3 = new interfaceSampler.PanelRound();
        labelPadSound3 = new javax.swing.JLabel();
        padSound4 = new interfaceSampler.PanelRound();
        labelPadSound4 = new javax.swing.JLabel();
        padSound5 = new interfaceSampler.PanelRound();
        labelPadSound5 = new javax.swing.JLabel();
        padSound6 = new interfaceSampler.PanelRound();
        labelPadSound6 = new javax.swing.JLabel();
        label_NumeroDelPAD_1 = new javax.swing.JLabel();
        label_NumeroDelPAD_2 = new javax.swing.JLabel();
        label_NumeroDelPAD_3 = new javax.swing.JLabel();
        label_NumeroDelPAD_4 = new javax.swing.JLabel();
        label_NumeroDelPAD_5 = new javax.swing.JLabel();
        label_NumeroDelPAD_6 = new javax.swing.JLabel();
        panelPrincipal_Tres = new javax.swing.JPanel();
        panelHeaderPads1 = new interfaceSampler.PanelRound();
        labelTitulo_PADS1 = new javax.swing.JLabel();
        botonSelecccionarSonido_Pad1 = new interfaceSampler.PanelRound();
        label_SeleccionarSonido1 = new javax.swing.JLabel();
        botonSelecccionarSonido_Pad2 = new interfaceSampler.PanelRound();
        label_SeleccionarSonido2 = new javax.swing.JLabel();
        botonSelecccionarSonido_Pad3 = new interfaceSampler.PanelRound();
        label_SeleccionarSonido3 = new javax.swing.JLabel();
        label_AjustePAD_1 = new javax.swing.JLabel();
        label_AjustePAD_2 = new javax.swing.JLabel();
        label_AjustePAD_3 = new javax.swing.JLabel();
        label_AjustePAD_4 = new javax.swing.JLabel();
        label_AjustePAD_5 = new javax.swing.JLabel();
        label_AjustePAD_6 = new javax.swing.JLabel();
        botonSelecccionarSonido_Pad4 = new interfaceSampler.PanelRound();
        label_SeleccionarSonido4 = new javax.swing.JLabel();
        botonSelecccionarSonido_Pad5 = new interfaceSampler.PanelRound();
        label_SeleccionarSonido5 = new javax.swing.JLabel();
        botonSelecccionarSonido_Pad6 = new interfaceSampler.PanelRound();
        label_SeleccionarSonido6 = new javax.swing.JLabel();
        ControlVolumen1 = new javax.swing.JSlider();
        ControlVolumen2 = new javax.swing.JSlider();
        ControlVolumen3 = new javax.swing.JSlider();
        ControlVolumen4 = new javax.swing.JSlider();
        ControlVolumen5 = new javax.swing.JSlider();
        ControlVolumen6 = new javax.swing.JSlider();
        botonConsultarSonido_Pad1 = new interfaceSampler.PanelRound();
        label_ConsultarSonido1 = new javax.swing.JLabel();
        botonConsultarSonido_Pad2 = new interfaceSampler.PanelRound();
        label_ConsultarSonido2 = new javax.swing.JLabel();
        botonConsultarSonido_Pad3 = new interfaceSampler.PanelRound();
        label_ConsultarSonido3 = new javax.swing.JLabel();
        botonConsultarSonido_Pad44 = new interfaceSampler.PanelRound();
        label_ConsultarSonido4 = new javax.swing.JLabel();
        botonConsultarSonido_Pad5 = new interfaceSampler.PanelRound();
        label_ConsultarSonido5 = new javax.swing.JLabel();
        botonConsultarSonido_Pad6 = new interfaceSampler.PanelRound();
        label_ConsultarSonido6 = new javax.swing.JLabel();
        botonEliminarSonido_Pad1 = new interfaceSampler.PanelRound();
        label_EliminarSonido1 = new javax.swing.JLabel();
        botonEliminarSonido_Pad2 = new interfaceSampler.PanelRound();
        label_EliminarSonido2 = new javax.swing.JLabel();
        botonEliminarSonido_Pad3 = new interfaceSampler.PanelRound();
        label_EliminarSonido3 = new javax.swing.JLabel();
        botonEliminarSonido_Pad4 = new interfaceSampler.PanelRound();
        label_EliminarSonido4 = new javax.swing.JLabel();
        botonEliminarSonido_Pad5 = new interfaceSampler.PanelRound();
        label_EliminarSonido5 = new javax.swing.JLabel();
        botonEliminarSonido_Pad66 = new interfaceSampler.PanelRound();
        label_EliminarSonido6 = new javax.swing.JLabel();
        panelPrincipal_Cuatro = new interfaceSampler.PanelRound();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        comboBoxCOM = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        labelEstado = new javax.swing.JLabel();
        panelPrincipal_Cinco = new interfaceSampler.PanelRound();
        label_TituloCancion = new javax.swing.JLabel();
        label_MusicTimeFinal = new javax.swing.JLabel();
        label_MusicTimeInicial = new javax.swing.JLabel();
        barraDeProgresoCancion = new javax.swing.JProgressBar();
        panelReiniciarCancion = new interfaceSampler.PanelRound();
        reiniciarCancion = new javax.swing.JLabel();
        panelPauseCancion = new interfaceSampler.PanelRound();
        pauseCancion = new javax.swing.JLabel();
        panelSubirCancion = new interfaceSampler.PanelRound();
        subirCancion = new javax.swing.JLabel();
        panelPlayCancion = new interfaceSampler.PanelRound();
        playCancion = new javax.swing.JLabel();
        sliderVolumenCancion = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PadLand");
        setResizable(false);

        bg.setBackground(new java.awt.Color(0, 0, 0));
        bg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelPrincipal_Uno.setBackground(new java.awt.Color(18, 18, 18));
        panelPrincipal_Uno.setRoundBottomLeft(25);
        panelPrincipal_Uno.setRoundBottomRight(25);
        panelPrincipal_Uno.setRoundTopLeft(25);
        panelPrincipal_Uno.setRoundTopRight(25);

        panelPerfil.setBackground(new java.awt.Color(18, 18, 18));

        label_PerfilSeleccionado.setBackground(new java.awt.Color(210, 210, 210));
        label_PerfilSeleccionado.setFont(new java.awt.Font("Roboto Medium", 0, 13)); // NOI18N
        label_PerfilSeleccionado.setForeground(new java.awt.Color(168, 168, 168));
        label_PerfilSeleccionado.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label_PerfilSeleccionado.setText("<html>Perfil Predeterminado<html>");

        label_ImageLibrary.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/libraryIcon.png"))); // NOI18N

        javax.swing.GroupLayout panelPerfilLayout = new javax.swing.GroupLayout(panelPerfil);
        panelPerfil.setLayout(panelPerfilLayout);
        panelPerfilLayout.setHorizontalGroup(
            panelPerfilLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPerfilLayout.createSequentialGroup()
                .addComponent(label_ImageLibrary, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_PerfilSeleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelPerfilLayout.setVerticalGroup(
            panelPerfilLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPerfilLayout.createSequentialGroup()
                .addGroup(panelPerfilLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(label_PerfilSeleccionado, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(label_ImageLibrary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        panelGuardar.setBackground(new java.awt.Color(31, 31, 31));
        panelGuardar.setRoundBottomLeft(30);
        panelGuardar.setRoundBottomRight(30);
        panelGuardar.setRoundTopLeft(30);
        panelGuardar.setRoundTopRight(30);

        botonGuardar.setBackground(new java.awt.Color(204, 204, 204));
        botonGuardar.setRoundBottomLeft(25);
        botonGuardar.setRoundBottomRight(25);
        botonGuardar.setRoundTopLeft(25);
        botonGuardar.setRoundTopRight(25);

        label_Guardar.setBackground(new java.awt.Color(210, 210, 210));
        label_Guardar.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        label_Guardar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_Guardar.setText("Guardar");
        label_Guardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_Guardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_GuardarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_GuardarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_GuardarMouseExited(evt);
            }
        });

        javax.swing.GroupLayout botonGuardarLayout = new javax.swing.GroupLayout(botonGuardar);
        botonGuardar.setLayout(botonGuardarLayout);
        botonGuardarLayout.setHorizontalGroup(
            botonGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label_Guardar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
        );
        botonGuardarLayout.setVerticalGroup(
            botonGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label_Guardar, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
        );

        label_TituloGuardar.setBackground(new java.awt.Color(210, 210, 210));
        label_TituloGuardar.setFont(new java.awt.Font("Roboto Black", 0, 15)); // NOI18N
        label_TituloGuardar.setForeground(new java.awt.Color(230, 230, 230));
        label_TituloGuardar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label_TituloGuardar.setText("Guardar Cambios");

        panelTextoGuardar.setBackground(new java.awt.Color(31, 31, 31));
        panelTextoGuardar.setRoundBottomLeft(30);
        panelTextoGuardar.setRoundBottomRight(30);
        panelTextoGuardar.setRoundTopLeft(30);
        panelTextoGuardar.setRoundTopRight(30);

        label_TextoGuardar.setBackground(new java.awt.Color(210, 210, 210));
        label_TextoGuardar.setFont(new java.awt.Font("Roboto Medium", 0, 13)); // NOI18N
        label_TextoGuardar.setForeground(new java.awt.Color(230, 230, 230));
        label_TextoGuardar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label_TextoGuardar.setText("<html>Guardar todos los cambios de los pads en el perfil seleccionado.<html>");

        javax.swing.GroupLayout panelTextoGuardarLayout = new javax.swing.GroupLayout(panelTextoGuardar);
        panelTextoGuardar.setLayout(panelTextoGuardarLayout);
        panelTextoGuardarLayout.setHorizontalGroup(
            panelTextoGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTextoGuardarLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(label_TextoGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelTextoGuardarLayout.setVerticalGroup(
            panelTextoGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTextoGuardarLayout.createSequentialGroup()
                .addComponent(label_TextoGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelGuardarLayout = new javax.swing.GroupLayout(panelGuardar);
        panelGuardar.setLayout(panelGuardarLayout);
        panelGuardarLayout.setHorizontalGroup(
            panelGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelTextoGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelGuardarLayout.createSequentialGroup()
                .addGroup(panelGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGuardarLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(botonGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGuardarLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(label_TituloGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelGuardarLayout.setVerticalGroup(
            panelGuardarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGuardarLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(label_TituloGuardar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelTextoGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        panelReestablecer.setBackground(new java.awt.Color(31, 31, 31));
        panelReestablecer.setRoundBottomLeft(30);
        panelReestablecer.setRoundBottomRight(30);
        panelReestablecer.setRoundTopLeft(30);
        panelReestablecer.setRoundTopRight(30);

        botonReestablecer.setBackground(new java.awt.Color(204, 204, 204));
        botonReestablecer.setRoundBottomLeft(25);
        botonReestablecer.setRoundBottomRight(25);
        botonReestablecer.setRoundTopLeft(25);
        botonReestablecer.setRoundTopRight(25);

        label_Reestablecer.setBackground(new java.awt.Color(204, 204, 204));
        label_Reestablecer.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        label_Reestablecer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_Reestablecer.setText("Reestablecer");
        label_Reestablecer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_Reestablecer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_ReestablecerMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_ReestablecerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_ReestablecerMouseExited(evt);
            }
        });

        javax.swing.GroupLayout botonReestablecerLayout = new javax.swing.GroupLayout(botonReestablecer);
        botonReestablecer.setLayout(botonReestablecerLayout);
        botonReestablecerLayout.setHorizontalGroup(
            botonReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label_Reestablecer, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
        );
        botonReestablecerLayout.setVerticalGroup(
            botonReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label_Reestablecer, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
        );

        label_TituloReestablecer.setBackground(new java.awt.Color(210, 210, 210));
        label_TituloReestablecer.setFont(new java.awt.Font("Roboto Black", 0, 15)); // NOI18N
        label_TituloReestablecer.setForeground(new java.awt.Color(230, 230, 230));
        label_TituloReestablecer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label_TituloReestablecer.setText("Reestablecer");

        panelTextoReestablecer.setBackground(new java.awt.Color(31, 31, 31));
        panelTextoReestablecer.setRoundBottomLeft(30);
        panelTextoReestablecer.setRoundBottomRight(30);
        panelTextoReestablecer.setRoundTopLeft(30);
        panelTextoReestablecer.setRoundTopRight(30);

        label_TextoReestablecer.setBackground(new java.awt.Color(210, 210, 210));
        label_TextoReestablecer.setFont(new java.awt.Font("Roboto Medium", 0, 13)); // NOI18N
        label_TextoReestablecer.setForeground(new java.awt.Color(230, 230, 230));
        label_TextoReestablecer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label_TextoReestablecer.setText("<html>Se reestablecen todos los pads.<html>");

        javax.swing.GroupLayout panelTextoReestablecerLayout = new javax.swing.GroupLayout(panelTextoReestablecer);
        panelTextoReestablecer.setLayout(panelTextoReestablecerLayout);
        panelTextoReestablecerLayout.setHorizontalGroup(
            panelTextoReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTextoReestablecerLayout.createSequentialGroup()
                .addComponent(label_TextoReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelTextoReestablecerLayout.setVerticalGroup(
            panelTextoReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label_TextoReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout panelReestablecerLayout = new javax.swing.GroupLayout(panelReestablecer);
        panelReestablecer.setLayout(panelReestablecerLayout);
        panelReestablecerLayout.setHorizontalGroup(
            panelReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelReestablecerLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTextoReestablecer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelReestablecerLayout.createSequentialGroup()
                        .addGroup(panelReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label_TituloReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        panelReestablecerLayout.setVerticalGroup(
            panelReestablecerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelReestablecerLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(label_TituloReestablecer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTextoReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout panelPrincipal_UnoLayout = new javax.swing.GroupLayout(panelPrincipal_Uno);
        panelPrincipal_Uno.setLayout(panelPrincipal_UnoLayout);
        panelPrincipal_UnoLayout.setHorizontalGroup(
            panelPrincipal_UnoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipal_UnoLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(panelPrincipal_UnoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelReestablecer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        panelPrincipal_UnoLayout.setVerticalGroup(
            panelPrincipal_UnoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipal_UnoLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(panelPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(panelReestablecer, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        bg.add(panelPrincipal_Uno, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 190, 412));

        panelPrincipal_Dos.setBackground(new java.awt.Color(18, 18, 18));
        panelPrincipal_Dos.setRoundBottomLeft(25);
        panelPrincipal_Dos.setRoundBottomRight(25);
        panelPrincipal_Dos.setRoundTopLeft(25);
        panelPrincipal_Dos.setRoundTopRight(25);

        panelHeaderPads.setBackground(new java.awt.Color(20, 20, 20));
        panelHeaderPads.setRoundTopLeft(25);
        panelHeaderPads.setRoundTopRight(25);

        labelTitulo_PADS.setBackground(new java.awt.Color(210, 210, 210));
        labelTitulo_PADS.setFont(new java.awt.Font("Roboto", 1, 24)); // NOI18N
        labelTitulo_PADS.setForeground(new java.awt.Color(210, 210, 210));
        labelTitulo_PADS.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelTitulo_PADS.setText("PADS");

        panelConsultarInfo.setBackground(new java.awt.Color(204, 204, 204));
        panelConsultarInfo.setRoundBottomLeft(40);
        panelConsultarInfo.setRoundBottomRight(40);
        panelConsultarInfo.setRoundTopLeft(40);
        panelConsultarInfo.setRoundTopRight(40);

        consultarInfo.setFont(new java.awt.Font("Roboto Black", 1, 18)); // NOI18N
        consultarInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        consultarInfo.setText("i");
        consultarInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        consultarInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                consultarInfoMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelConsultarInfoLayout = new javax.swing.GroupLayout(panelConsultarInfo);
        panelConsultarInfo.setLayout(panelConsultarInfoLayout);
        panelConsultarInfoLayout.setHorizontalGroup(
            panelConsultarInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consultarInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );
        panelConsultarInfoLayout.setVerticalGroup(
            panelConsultarInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consultarInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelHeaderPadsLayout = new javax.swing.GroupLayout(panelHeaderPads);
        panelHeaderPads.setLayout(panelHeaderPadsLayout);
        panelHeaderPadsLayout.setHorizontalGroup(
            panelHeaderPadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderPadsLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(labelTitulo_PADS, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelConsultarInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );
        panelHeaderPadsLayout.setVerticalGroup(
            panelHeaderPadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderPadsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeaderPadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelConsultarInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelHeaderPadsLayout.createSequentialGroup()
                        .addComponent(labelTitulo_PADS)
                        .addGap(0, 1, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jSeparator1.setForeground(new java.awt.Color(204, 204, 204));

        labelPerfil_Seleccionado.setBackground(new java.awt.Color(107, 107, 107));
        labelPerfil_Seleccionado.setFont(new java.awt.Font("Roboto Medium", 2, 10)); // NOI18N
        labelPerfil_Seleccionado.setForeground(new java.awt.Color(107, 107, 107));
        labelPerfil_Seleccionado.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPerfil_Seleccionado.setText("© 2024 José Serpa  -  Jairo Gamarra");

        padSound1.setBackground(new java.awt.Color(119, 55, 149));
        padSound1.setRoundBottomLeft(30);
        padSound1.setRoundBottomRight(30);
        padSound1.setRoundTopLeft(30);
        padSound1.setRoundTopRight(30);

        labelPadSound1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout padSound1Layout = new javax.swing.GroupLayout(padSound1);
        padSound1.setLayout(padSound1Layout);
        padSound1Layout.setHorizontalGroup(
            padSound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelPadSound1, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        );
        padSound1Layout.setVerticalGroup(
            padSound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelPadSound1, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
        );

        padSound2.setBackground(new java.awt.Color(119, 55, 149));
        padSound2.setRoundBottomLeft(30);
        padSound2.setRoundBottomRight(30);
        padSound2.setRoundTopLeft(30);
        padSound2.setRoundTopRight(30);

        labelPadSound2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout padSound2Layout = new javax.swing.GroupLayout(padSound2);
        padSound2.setLayout(padSound2Layout);
        padSound2Layout.setHorizontalGroup(
            padSound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound2, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        padSound2Layout.setVerticalGroup(
            padSound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        padSound3.setBackground(new java.awt.Color(235, 30, 50));
        padSound3.setRoundBottomLeft(30);
        padSound3.setRoundBottomRight(30);
        padSound3.setRoundTopLeft(30);
        padSound3.setRoundTopRight(30);

        labelPadSound3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout padSound3Layout = new javax.swing.GroupLayout(padSound3);
        padSound3.setLayout(padSound3Layout);
        padSound3Layout.setHorizontalGroup(
            padSound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound3, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        padSound3Layout.setVerticalGroup(
            padSound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound3, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        padSound4.setBackground(new java.awt.Color(235, 30, 50));
        padSound4.setRoundBottomLeft(30);
        padSound4.setRoundBottomRight(30);
        padSound4.setRoundTopLeft(30);
        padSound4.setRoundTopRight(30);

        labelPadSound4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout padSound4Layout = new javax.swing.GroupLayout(padSound4);
        padSound4.setLayout(padSound4Layout);
        padSound4Layout.setHorizontalGroup(
            padSound4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound4, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        padSound4Layout.setVerticalGroup(
            padSound4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound4, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        padSound5.setBackground(new java.awt.Color(30, 215, 96));
        padSound5.setRoundBottomLeft(30);
        padSound5.setRoundBottomRight(30);
        padSound5.setRoundTopLeft(30);
        padSound5.setRoundTopRight(30);

        labelPadSound5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout padSound5Layout = new javax.swing.GroupLayout(padSound5);
        padSound5.setLayout(padSound5Layout);
        padSound5Layout.setHorizontalGroup(
            padSound5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound5, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        padSound5Layout.setVerticalGroup(
            padSound5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, padSound5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelPadSound5, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        padSound6.setBackground(new java.awt.Color(30, 215, 96));
        padSound6.setRoundBottomLeft(30);
        padSound6.setRoundBottomRight(30);
        padSound6.setRoundTopLeft(30);
        padSound6.setRoundTopRight(30);

        labelPadSound6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout padSound6Layout = new javax.swing.GroupLayout(padSound6);
        padSound6.setLayout(padSound6Layout);
        padSound6Layout.setHorizontalGroup(
            padSound6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelPadSound6, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        );
        padSound6Layout.setVerticalGroup(
            padSound6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelPadSound6, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
        );

        label_NumeroDelPAD_1.setBackground(new java.awt.Color(210, 210, 210));
        label_NumeroDelPAD_1.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        label_NumeroDelPAD_1.setForeground(new java.awt.Color(66, 66, 66));
        label_NumeroDelPAD_1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_NumeroDelPAD_1.setText("PAD 1");

        label_NumeroDelPAD_2.setBackground(new java.awt.Color(210, 210, 210));
        label_NumeroDelPAD_2.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        label_NumeroDelPAD_2.setForeground(new java.awt.Color(66, 66, 66));
        label_NumeroDelPAD_2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_NumeroDelPAD_2.setText("PAD 2");

        label_NumeroDelPAD_3.setBackground(new java.awt.Color(210, 210, 210));
        label_NumeroDelPAD_3.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        label_NumeroDelPAD_3.setForeground(new java.awt.Color(66, 66, 66));
        label_NumeroDelPAD_3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_NumeroDelPAD_3.setText("PAD 3");

        label_NumeroDelPAD_4.setBackground(new java.awt.Color(210, 210, 210));
        label_NumeroDelPAD_4.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        label_NumeroDelPAD_4.setForeground(new java.awt.Color(66, 66, 66));
        label_NumeroDelPAD_4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_NumeroDelPAD_4.setText("PAD 4");

        label_NumeroDelPAD_5.setBackground(new java.awt.Color(210, 210, 210));
        label_NumeroDelPAD_5.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        label_NumeroDelPAD_5.setForeground(new java.awt.Color(66, 66, 66));
        label_NumeroDelPAD_5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_NumeroDelPAD_5.setText("PAD 5");

        label_NumeroDelPAD_6.setBackground(new java.awt.Color(210, 210, 210));
        label_NumeroDelPAD_6.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        label_NumeroDelPAD_6.setForeground(new java.awt.Color(66, 66, 66));
        label_NumeroDelPAD_6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_NumeroDelPAD_6.setText("PAD 6");

        javax.swing.GroupLayout panelPrincipal_DosLayout = new javax.swing.GroupLayout(panelPrincipal_Dos);
        panelPrincipal_Dos.setLayout(panelPrincipal_DosLayout);
        panelPrincipal_DosLayout.setHorizontalGroup(
            panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelHeaderPads, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelPrincipal_DosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(panelPrincipal_DosLayout.createSequentialGroup()
                        .addComponent(labelPerfil_Seleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipal_DosLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipal_DosLayout.createSequentialGroup()
                        .addComponent(padSound1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(padSound3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(padSound5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipal_DosLayout.createSequentialGroup()
                        .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelPrincipal_DosLayout.createSequentialGroup()
                                .addComponent(padSound2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(label_NumeroDelPAD_2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label_NumeroDelPAD_1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(label_NumeroDelPAD_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label_NumeroDelPAD_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(padSound4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(padSound6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label_NumeroDelPAD_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label_NumeroDelPAD_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(34, 34, 34))
        );
        panelPrincipal_DosLayout.setVerticalGroup(
            panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipal_DosLayout.createSequentialGroup()
                .addComponent(panelHeaderPads, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(padSound1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(padSound3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(padSound5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label_NumeroDelPAD_1)
                    .addComponent(label_NumeroDelPAD_3)
                    .addComponent(label_NumeroDelPAD_5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(padSound6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(padSound2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(padSound4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipal_DosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label_NumeroDelPAD_4)
                    .addComponent(label_NumeroDelPAD_6)
                    .addComponent(label_NumeroDelPAD_2))
                .addGap(16, 16, 16)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPerfil_Seleccionado, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bg.add(panelPrincipal_Dos, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 20, 410, 412));

        panelPrincipal_Tres.setBackground(new java.awt.Color(18, 18, 18));
        panelPrincipal_Tres.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.gray, java.awt.Color.gray, java.awt.Color.darkGray, java.awt.Color.darkGray));

        panelHeaderPads1.setBackground(new java.awt.Color(20, 20, 20));
        panelHeaderPads1.setRoundBottomLeft(10);
        panelHeaderPads1.setRoundBottomRight(10);
        panelHeaderPads1.setRoundTopLeft(10);
        panelHeaderPads1.setRoundTopRight(10);

        labelTitulo_PADS1.setBackground(new java.awt.Color(210, 210, 210));
        labelTitulo_PADS1.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        labelTitulo_PADS1.setForeground(new java.awt.Color(210, 210, 210));
        labelTitulo_PADS1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelTitulo_PADS1.setText("Configuración de Pads");

        javax.swing.GroupLayout panelHeaderPads1Layout = new javax.swing.GroupLayout(panelHeaderPads1);
        panelHeaderPads1.setLayout(panelHeaderPads1Layout);
        panelHeaderPads1Layout.setHorizontalGroup(
            panelHeaderPads1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderPads1Layout.createSequentialGroup()
                .addComponent(labelTitulo_PADS1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelHeaderPads1Layout.setVerticalGroup(
            panelHeaderPads1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderPads1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo_PADS1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        botonSelecccionarSonido_Pad1.setBackground(new java.awt.Color(119, 55, 149));
        botonSelecccionarSonido_Pad1.setRoundBottomLeft(25);
        botonSelecccionarSonido_Pad1.setRoundBottomRight(25);
        botonSelecccionarSonido_Pad1.setRoundTopLeft(25);
        botonSelecccionarSonido_Pad1.setRoundTopRight(25);

        label_SeleccionarSonido1.setBackground(new java.awt.Color(210, 210, 210));
        label_SeleccionarSonido1.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_SeleccionarSonido1.setForeground(new java.awt.Color(255, 255, 255));
        label_SeleccionarSonido1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_SeleccionarSonido1.setText("+");
        label_SeleccionarSonido1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonSelecccionarSonido_Pad1Layout = new javax.swing.GroupLayout(botonSelecccionarSonido_Pad1);
        botonSelecccionarSonido_Pad1.setLayout(botonSelecccionarSonido_Pad1Layout);
        botonSelecccionarSonido_Pad1Layout.setHorizontalGroup(
            botonSelecccionarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(botonSelecccionarSonido_Pad1Layout.createSequentialGroup()
                    .addComponent(label_SeleccionarSonido1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        botonSelecccionarSonido_Pad1Layout.setVerticalGroup(
            botonSelecccionarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_SeleccionarSonido1, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonSelecccionarSonido_Pad2.setBackground(new java.awt.Color(119, 55, 149));
        botonSelecccionarSonido_Pad2.setRoundBottomLeft(25);
        botonSelecccionarSonido_Pad2.setRoundBottomRight(25);
        botonSelecccionarSonido_Pad2.setRoundTopLeft(25);
        botonSelecccionarSonido_Pad2.setRoundTopRight(25);

        label_SeleccionarSonido2.setBackground(new java.awt.Color(210, 210, 210));
        label_SeleccionarSonido2.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_SeleccionarSonido2.setForeground(new java.awt.Color(255, 255, 255));
        label_SeleccionarSonido2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_SeleccionarSonido2.setText("+");
        label_SeleccionarSonido2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonSelecccionarSonido_Pad2Layout = new javax.swing.GroupLayout(botonSelecccionarSonido_Pad2);
        botonSelecccionarSonido_Pad2.setLayout(botonSelecccionarSonido_Pad2Layout);
        botonSelecccionarSonido_Pad2Layout.setHorizontalGroup(
            botonSelecccionarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(botonSelecccionarSonido_Pad2Layout.createSequentialGroup()
                    .addComponent(label_SeleccionarSonido2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        botonSelecccionarSonido_Pad2Layout.setVerticalGroup(
            botonSelecccionarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_SeleccionarSonido2, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonSelecccionarSonido_Pad3.setBackground(new java.awt.Color(235, 30, 50));
        botonSelecccionarSonido_Pad3.setRoundBottomLeft(25);
        botonSelecccionarSonido_Pad3.setRoundBottomRight(25);
        botonSelecccionarSonido_Pad3.setRoundTopLeft(25);
        botonSelecccionarSonido_Pad3.setRoundTopRight(25);

        label_SeleccionarSonido3.setBackground(new java.awt.Color(210, 210, 210));
        label_SeleccionarSonido3.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_SeleccionarSonido3.setForeground(new java.awt.Color(255, 255, 255));
        label_SeleccionarSonido3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_SeleccionarSonido3.setText("+");
        label_SeleccionarSonido3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonSelecccionarSonido_Pad3Layout = new javax.swing.GroupLayout(botonSelecccionarSonido_Pad3);
        botonSelecccionarSonido_Pad3.setLayout(botonSelecccionarSonido_Pad3Layout);
        botonSelecccionarSonido_Pad3Layout.setHorizontalGroup(
            botonSelecccionarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(botonSelecccionarSonido_Pad3Layout.createSequentialGroup()
                    .addComponent(label_SeleccionarSonido3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        botonSelecccionarSonido_Pad3Layout.setVerticalGroup(
            botonSelecccionarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_SeleccionarSonido3, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        label_AjustePAD_1.setBackground(new java.awt.Color(210, 210, 210));
        label_AjustePAD_1.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        label_AjustePAD_1.setForeground(new java.awt.Color(230, 230, 230));
        label_AjustePAD_1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_AjustePAD_1.setText("PAD 1");

        label_AjustePAD_2.setBackground(new java.awt.Color(210, 210, 210));
        label_AjustePAD_2.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        label_AjustePAD_2.setForeground(new java.awt.Color(230, 230, 230));
        label_AjustePAD_2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_AjustePAD_2.setText("PAD 2");

        label_AjustePAD_3.setBackground(new java.awt.Color(210, 210, 210));
        label_AjustePAD_3.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        label_AjustePAD_3.setForeground(new java.awt.Color(230, 230, 230));
        label_AjustePAD_3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_AjustePAD_3.setText("PAD 3");

        label_AjustePAD_4.setBackground(new java.awt.Color(210, 210, 210));
        label_AjustePAD_4.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        label_AjustePAD_4.setForeground(new java.awt.Color(230, 230, 230));
        label_AjustePAD_4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_AjustePAD_4.setText("PAD 4");

        label_AjustePAD_5.setBackground(new java.awt.Color(210, 210, 210));
        label_AjustePAD_5.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        label_AjustePAD_5.setForeground(new java.awt.Color(230, 230, 230));
        label_AjustePAD_5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_AjustePAD_5.setText("PAD 5");

        label_AjustePAD_6.setBackground(new java.awt.Color(210, 210, 210));
        label_AjustePAD_6.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        label_AjustePAD_6.setForeground(new java.awt.Color(230, 230, 230));
        label_AjustePAD_6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_AjustePAD_6.setText("PAD 6");

        botonSelecccionarSonido_Pad4.setBackground(new java.awt.Color(235, 30, 50));
        botonSelecccionarSonido_Pad4.setRoundBottomLeft(25);
        botonSelecccionarSonido_Pad4.setRoundBottomRight(25);
        botonSelecccionarSonido_Pad4.setRoundTopLeft(25);
        botonSelecccionarSonido_Pad4.setRoundTopRight(25);

        label_SeleccionarSonido4.setBackground(new java.awt.Color(210, 210, 210));
        label_SeleccionarSonido4.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_SeleccionarSonido4.setForeground(new java.awt.Color(255, 255, 255));
        label_SeleccionarSonido4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_SeleccionarSonido4.setText("+");
        label_SeleccionarSonido4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonSelecccionarSonido_Pad4Layout = new javax.swing.GroupLayout(botonSelecccionarSonido_Pad4);
        botonSelecccionarSonido_Pad4.setLayout(botonSelecccionarSonido_Pad4Layout);
        botonSelecccionarSonido_Pad4Layout.setHorizontalGroup(
            botonSelecccionarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(botonSelecccionarSonido_Pad4Layout.createSequentialGroup()
                    .addComponent(label_SeleccionarSonido4, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        botonSelecccionarSonido_Pad4Layout.setVerticalGroup(
            botonSelecccionarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_SeleccionarSonido4, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonSelecccionarSonido_Pad5.setBackground(new java.awt.Color(30, 215, 96));
        botonSelecccionarSonido_Pad5.setRoundBottomLeft(25);
        botonSelecccionarSonido_Pad5.setRoundBottomRight(25);
        botonSelecccionarSonido_Pad5.setRoundTopLeft(25);
        botonSelecccionarSonido_Pad5.setRoundTopRight(25);

        label_SeleccionarSonido5.setBackground(new java.awt.Color(210, 210, 210));
        label_SeleccionarSonido5.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_SeleccionarSonido5.setForeground(new java.awt.Color(255, 255, 255));
        label_SeleccionarSonido5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_SeleccionarSonido5.setText("+");
        label_SeleccionarSonido5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonSelecccionarSonido_Pad5Layout = new javax.swing.GroupLayout(botonSelecccionarSonido_Pad5);
        botonSelecccionarSonido_Pad5.setLayout(botonSelecccionarSonido_Pad5Layout);
        botonSelecccionarSonido_Pad5Layout.setHorizontalGroup(
            botonSelecccionarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(botonSelecccionarSonido_Pad5Layout.createSequentialGroup()
                    .addComponent(label_SeleccionarSonido5, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        botonSelecccionarSonido_Pad5Layout.setVerticalGroup(
            botonSelecccionarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_SeleccionarSonido5, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonSelecccionarSonido_Pad6.setBackground(new java.awt.Color(30, 215, 96));
        botonSelecccionarSonido_Pad6.setRoundBottomLeft(25);
        botonSelecccionarSonido_Pad6.setRoundBottomRight(25);
        botonSelecccionarSonido_Pad6.setRoundTopLeft(25);
        botonSelecccionarSonido_Pad6.setRoundTopRight(25);

        label_SeleccionarSonido6.setBackground(new java.awt.Color(210, 210, 210));
        label_SeleccionarSonido6.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_SeleccionarSonido6.setForeground(new java.awt.Color(255, 255, 255));
        label_SeleccionarSonido6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_SeleccionarSonido6.setText("+");
        label_SeleccionarSonido6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonSelecccionarSonido_Pad6Layout = new javax.swing.GroupLayout(botonSelecccionarSonido_Pad6);
        botonSelecccionarSonido_Pad6.setLayout(botonSelecccionarSonido_Pad6Layout);
        botonSelecccionarSonido_Pad6Layout.setHorizontalGroup(
            botonSelecccionarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(botonSelecccionarSonido_Pad6Layout.createSequentialGroup()
                    .addComponent(label_SeleccionarSonido6, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        botonSelecccionarSonido_Pad6Layout.setVerticalGroup(
            botonSelecccionarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonSelecccionarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_SeleccionarSonido6, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        ControlVolumen1.setForeground(new java.awt.Color(255, 255, 255));
        ControlVolumen1.setOrientation(javax.swing.JSlider.VERTICAL);

        ControlVolumen2.setForeground(new java.awt.Color(255, 255, 255));
        ControlVolumen2.setOrientation(javax.swing.JSlider.VERTICAL);

        ControlVolumen3.setForeground(new java.awt.Color(255, 255, 255));
        ControlVolumen3.setOrientation(javax.swing.JSlider.VERTICAL);

        ControlVolumen4.setForeground(new java.awt.Color(255, 255, 255));
        ControlVolumen4.setOrientation(javax.swing.JSlider.VERTICAL);

        ControlVolumen5.setForeground(new java.awt.Color(255, 255, 255));
        ControlVolumen5.setOrientation(javax.swing.JSlider.VERTICAL);

        ControlVolumen6.setForeground(new java.awt.Color(255, 255, 255));
        ControlVolumen6.setOrientation(javax.swing.JSlider.VERTICAL);

        botonConsultarSonido_Pad1.setBackground(new java.awt.Color(119, 55, 149));
        botonConsultarSonido_Pad1.setRoundBottomLeft(25);
        botonConsultarSonido_Pad1.setRoundBottomRight(25);
        botonConsultarSonido_Pad1.setRoundTopLeft(25);
        botonConsultarSonido_Pad1.setRoundTopRight(25);

        label_ConsultarSonido1.setBackground(new java.awt.Color(210, 210, 210));
        label_ConsultarSonido1.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_ConsultarSonido1.setForeground(new java.awt.Color(255, 255, 255));
        label_ConsultarSonido1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_ConsultarSonido1.setText("i");
        label_ConsultarSonido1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonConsultarSonido_Pad1Layout = new javax.swing.GroupLayout(botonConsultarSonido_Pad1);
        botonConsultarSonido_Pad1.setLayout(botonConsultarSonido_Pad1Layout);
        botonConsultarSonido_Pad1Layout.setHorizontalGroup(
            botonConsultarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido1, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonConsultarSonido_Pad1Layout.setVerticalGroup(
            botonConsultarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido1, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonConsultarSonido_Pad2.setBackground(new java.awt.Color(119, 55, 149));
        botonConsultarSonido_Pad2.setRoundBottomLeft(25);
        botonConsultarSonido_Pad2.setRoundBottomRight(25);
        botonConsultarSonido_Pad2.setRoundTopLeft(25);
        botonConsultarSonido_Pad2.setRoundTopRight(25);

        label_ConsultarSonido2.setBackground(new java.awt.Color(210, 210, 210));
        label_ConsultarSonido2.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_ConsultarSonido2.setForeground(new java.awt.Color(255, 255, 255));
        label_ConsultarSonido2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_ConsultarSonido2.setText("i");
        label_ConsultarSonido2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonConsultarSonido_Pad2Layout = new javax.swing.GroupLayout(botonConsultarSonido_Pad2);
        botonConsultarSonido_Pad2.setLayout(botonConsultarSonido_Pad2Layout);
        botonConsultarSonido_Pad2Layout.setHorizontalGroup(
            botonConsultarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido2, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonConsultarSonido_Pad2Layout.setVerticalGroup(
            botonConsultarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido2, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonConsultarSonido_Pad3.setBackground(new java.awt.Color(235, 30, 50));
        botonConsultarSonido_Pad3.setRoundBottomLeft(25);
        botonConsultarSonido_Pad3.setRoundBottomRight(25);
        botonConsultarSonido_Pad3.setRoundTopLeft(25);
        botonConsultarSonido_Pad3.setRoundTopRight(25);

        label_ConsultarSonido3.setBackground(new java.awt.Color(210, 210, 210));
        label_ConsultarSonido3.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_ConsultarSonido3.setForeground(new java.awt.Color(255, 255, 255));
        label_ConsultarSonido3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_ConsultarSonido3.setText("i");
        label_ConsultarSonido3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonConsultarSonido_Pad3Layout = new javax.swing.GroupLayout(botonConsultarSonido_Pad3);
        botonConsultarSonido_Pad3.setLayout(botonConsultarSonido_Pad3Layout);
        botonConsultarSonido_Pad3Layout.setHorizontalGroup(
            botonConsultarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido3, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonConsultarSonido_Pad3Layout.setVerticalGroup(
            botonConsultarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido3, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonConsultarSonido_Pad44.setBackground(new java.awt.Color(235, 30, 50));
        botonConsultarSonido_Pad44.setRoundBottomLeft(25);
        botonConsultarSonido_Pad44.setRoundBottomRight(25);
        botonConsultarSonido_Pad44.setRoundTopLeft(25);
        botonConsultarSonido_Pad44.setRoundTopRight(25);

        label_ConsultarSonido4.setBackground(new java.awt.Color(210, 210, 210));
        label_ConsultarSonido4.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_ConsultarSonido4.setForeground(new java.awt.Color(255, 255, 255));
        label_ConsultarSonido4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_ConsultarSonido4.setText("i");
        label_ConsultarSonido4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonConsultarSonido_Pad44Layout = new javax.swing.GroupLayout(botonConsultarSonido_Pad44);
        botonConsultarSonido_Pad44.setLayout(botonConsultarSonido_Pad44Layout);
        botonConsultarSonido_Pad44Layout.setHorizontalGroup(
            botonConsultarSonido_Pad44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido4, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonConsultarSonido_Pad44Layout.setVerticalGroup(
            botonConsultarSonido_Pad44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido4, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonConsultarSonido_Pad5.setBackground(new java.awt.Color(30, 215, 96));
        botonConsultarSonido_Pad5.setRoundBottomLeft(25);
        botonConsultarSonido_Pad5.setRoundBottomRight(25);
        botonConsultarSonido_Pad5.setRoundTopLeft(25);
        botonConsultarSonido_Pad5.setRoundTopRight(25);

        label_ConsultarSonido5.setBackground(new java.awt.Color(210, 210, 210));
        label_ConsultarSonido5.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_ConsultarSonido5.setForeground(new java.awt.Color(255, 255, 255));
        label_ConsultarSonido5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_ConsultarSonido5.setText("i");
        label_ConsultarSonido5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonConsultarSonido_Pad5Layout = new javax.swing.GroupLayout(botonConsultarSonido_Pad5);
        botonConsultarSonido_Pad5.setLayout(botonConsultarSonido_Pad5Layout);
        botonConsultarSonido_Pad5Layout.setHorizontalGroup(
            botonConsultarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido5, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonConsultarSonido_Pad5Layout.setVerticalGroup(
            botonConsultarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido5, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonConsultarSonido_Pad6.setBackground(new java.awt.Color(30, 215, 96));
        botonConsultarSonido_Pad6.setRoundBottomLeft(25);
        botonConsultarSonido_Pad6.setRoundBottomRight(25);
        botonConsultarSonido_Pad6.setRoundTopLeft(25);
        botonConsultarSonido_Pad6.setRoundTopRight(25);

        label_ConsultarSonido6.setBackground(new java.awt.Color(210, 210, 210));
        label_ConsultarSonido6.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_ConsultarSonido6.setForeground(new java.awt.Color(255, 255, 255));
        label_ConsultarSonido6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_ConsultarSonido6.setText("i");
        label_ConsultarSonido6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonConsultarSonido_Pad6Layout = new javax.swing.GroupLayout(botonConsultarSonido_Pad6);
        botonConsultarSonido_Pad6.setLayout(botonConsultarSonido_Pad6Layout);
        botonConsultarSonido_Pad6Layout.setHorizontalGroup(
            botonConsultarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido6, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonConsultarSonido_Pad6Layout.setVerticalGroup(
            botonConsultarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonConsultarSonido_Pad6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_ConsultarSonido6, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonEliminarSonido_Pad1.setBackground(new java.awt.Color(119, 55, 149));
        botonEliminarSonido_Pad1.setRoundBottomLeft(25);
        botonEliminarSonido_Pad1.setRoundBottomRight(25);
        botonEliminarSonido_Pad1.setRoundTopLeft(25);
        botonEliminarSonido_Pad1.setRoundTopRight(25);

        label_EliminarSonido1.setBackground(new java.awt.Color(210, 210, 210));
        label_EliminarSonido1.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_EliminarSonido1.setForeground(new java.awt.Color(255, 255, 255));
        label_EliminarSonido1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_EliminarSonido1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/image-removebg-preview 1.png"))); // NOI18N
        label_EliminarSonido1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonEliminarSonido_Pad1Layout = new javax.swing.GroupLayout(botonEliminarSonido_Pad1);
        botonEliminarSonido_Pad1.setLayout(botonEliminarSonido_Pad1Layout);
        botonEliminarSonido_Pad1Layout.setHorizontalGroup(
            botonEliminarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido1, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonEliminarSonido_Pad1Layout.setVerticalGroup(
            botonEliminarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido1, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonEliminarSonido_Pad2.setBackground(new java.awt.Color(119, 55, 149));
        botonEliminarSonido_Pad2.setRoundBottomLeft(25);
        botonEliminarSonido_Pad2.setRoundBottomRight(25);
        botonEliminarSonido_Pad2.setRoundTopLeft(25);
        botonEliminarSonido_Pad2.setRoundTopRight(25);

        label_EliminarSonido2.setBackground(new java.awt.Color(210, 210, 210));
        label_EliminarSonido2.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_EliminarSonido2.setForeground(new java.awt.Color(255, 255, 255));
        label_EliminarSonido2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_EliminarSonido2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/image-removebg-preview 1.png"))); // NOI18N
        label_EliminarSonido2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonEliminarSonido_Pad2Layout = new javax.swing.GroupLayout(botonEliminarSonido_Pad2);
        botonEliminarSonido_Pad2.setLayout(botonEliminarSonido_Pad2Layout);
        botonEliminarSonido_Pad2Layout.setHorizontalGroup(
            botonEliminarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido2, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonEliminarSonido_Pad2Layout.setVerticalGroup(
            botonEliminarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido2, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonEliminarSonido_Pad3.setBackground(new java.awt.Color(235, 30, 50));
        botonEliminarSonido_Pad3.setRoundBottomLeft(25);
        botonEliminarSonido_Pad3.setRoundBottomRight(25);
        botonEliminarSonido_Pad3.setRoundTopLeft(25);
        botonEliminarSonido_Pad3.setRoundTopRight(25);

        label_EliminarSonido3.setBackground(new java.awt.Color(210, 210, 210));
        label_EliminarSonido3.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_EliminarSonido3.setForeground(new java.awt.Color(255, 255, 255));
        label_EliminarSonido3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_EliminarSonido3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/image-removebg-preview 1.png"))); // NOI18N
        label_EliminarSonido3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonEliminarSonido_Pad3Layout = new javax.swing.GroupLayout(botonEliminarSonido_Pad3);
        botonEliminarSonido_Pad3.setLayout(botonEliminarSonido_Pad3Layout);
        botonEliminarSonido_Pad3Layout.setHorizontalGroup(
            botonEliminarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido3, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonEliminarSonido_Pad3Layout.setVerticalGroup(
            botonEliminarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido3, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonEliminarSonido_Pad4.setBackground(new java.awt.Color(235, 30, 50));
        botonEliminarSonido_Pad4.setRoundBottomLeft(25);
        botonEliminarSonido_Pad4.setRoundBottomRight(25);
        botonEliminarSonido_Pad4.setRoundTopLeft(25);
        botonEliminarSonido_Pad4.setRoundTopRight(25);

        label_EliminarSonido4.setBackground(new java.awt.Color(210, 210, 210));
        label_EliminarSonido4.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_EliminarSonido4.setForeground(new java.awt.Color(255, 255, 255));
        label_EliminarSonido4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_EliminarSonido4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/image-removebg-preview 1.png"))); // NOI18N
        label_EliminarSonido4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonEliminarSonido_Pad4Layout = new javax.swing.GroupLayout(botonEliminarSonido_Pad4);
        botonEliminarSonido_Pad4.setLayout(botonEliminarSonido_Pad4Layout);
        botonEliminarSonido_Pad4Layout.setHorizontalGroup(
            botonEliminarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido4, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonEliminarSonido_Pad4Layout.setVerticalGroup(
            botonEliminarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido4, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonEliminarSonido_Pad5.setBackground(new java.awt.Color(30, 215, 96));
        botonEliminarSonido_Pad5.setRoundBottomLeft(25);
        botonEliminarSonido_Pad5.setRoundBottomRight(25);
        botonEliminarSonido_Pad5.setRoundTopLeft(25);
        botonEliminarSonido_Pad5.setRoundTopRight(25);

        label_EliminarSonido5.setBackground(new java.awt.Color(210, 210, 210));
        label_EliminarSonido5.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_EliminarSonido5.setForeground(new java.awt.Color(255, 255, 255));
        label_EliminarSonido5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_EliminarSonido5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/image-removebg-preview 1.png"))); // NOI18N
        label_EliminarSonido5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonEliminarSonido_Pad5Layout = new javax.swing.GroupLayout(botonEliminarSonido_Pad5);
        botonEliminarSonido_Pad5.setLayout(botonEliminarSonido_Pad5Layout);
        botonEliminarSonido_Pad5Layout.setHorizontalGroup(
            botonEliminarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido5, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonEliminarSonido_Pad5Layout.setVerticalGroup(
            botonEliminarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido5, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        botonEliminarSonido_Pad66.setBackground(new java.awt.Color(30, 215, 96));
        botonEliminarSonido_Pad66.setRoundBottomLeft(25);
        botonEliminarSonido_Pad66.setRoundBottomRight(25);
        botonEliminarSonido_Pad66.setRoundTopLeft(25);
        botonEliminarSonido_Pad66.setRoundTopRight(25);

        label_EliminarSonido6.setBackground(new java.awt.Color(210, 210, 210));
        label_EliminarSonido6.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        label_EliminarSonido6.setForeground(new java.awt.Color(255, 255, 255));
        label_EliminarSonido6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_EliminarSonido6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/image-removebg-preview 1.png"))); // NOI18N
        label_EliminarSonido6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout botonEliminarSonido_Pad66Layout = new javax.swing.GroupLayout(botonEliminarSonido_Pad66);
        botonEliminarSonido_Pad66.setLayout(botonEliminarSonido_Pad66Layout);
        botonEliminarSonido_Pad66Layout.setHorizontalGroup(
            botonEliminarSonido_Pad66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido6, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
        botonEliminarSonido_Pad66Layout.setVerticalGroup(
            botonEliminarSonido_Pad66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
            .addGroup(botonEliminarSonido_Pad66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_EliminarSonido6, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelPrincipal_TresLayout = new javax.swing.GroupLayout(panelPrincipal_Tres);
        panelPrincipal_Tres.setLayout(panelPrincipal_TresLayout);
        panelPrincipal_TresLayout.setHorizontalGroup(
            panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelHeaderPads1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                            .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(botonSelecccionarSonido_Pad3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(botonConsultarSonido_Pad3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(botonEliminarSonido_Pad3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(ControlVolumen3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelPrincipal_TresLayout.createSequentialGroup()
                            .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(botonSelecccionarSonido_Pad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(botonConsultarSonido_Pad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(botonEliminarSonido_Pad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(ControlVolumen1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(label_AjustePAD_3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                        .addComponent(label_AjustePAD_5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label_AjustePAD_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(botonSelecccionarSonido_Pad5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonConsultarSonido_Pad5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonEliminarSonido_Pad5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ControlVolumen5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(58, 58, 58)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(botonSelecccionarSonido_Pad2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonConsultarSonido_Pad2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonEliminarSonido_Pad2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ControlVolumen2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(label_AjustePAD_2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(label_AjustePAD_6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label_AjustePAD_4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelPrincipal_TresLayout.createSequentialGroup()
                            .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(botonSelecccionarSonido_Pad4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(botonConsultarSonido_Pad44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(botonEliminarSonido_Pad4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(ControlVolumen4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(botonSelecccionarSonido_Pad6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonConsultarSonido_Pad6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonEliminarSonido_Pad66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ControlVolumen6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        panelPrincipal_TresLayout.setVerticalGroup(
            panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                .addComponent(panelHeaderPads1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label_AjustePAD_1)
                    .addComponent(label_AjustePAD_2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ControlVolumen2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addComponent(botonSelecccionarSonido_Pad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonConsultarSonido_Pad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonEliminarSonido_Pad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addComponent(botonSelecccionarSonido_Pad2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonConsultarSonido_Pad2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonEliminarSonido_Pad2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ControlVolumen1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label_AjustePAD_3)
                    .addComponent(label_AjustePAD_4, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ControlVolumen3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipal_TresLayout.createSequentialGroup()
                        .addComponent(botonSelecccionarSonido_Pad3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(botonConsultarSonido_Pad3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonEliminarSonido_Pad3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addComponent(botonSelecccionarSonido_Pad4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonConsultarSonido_Pad44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonEliminarSonido_Pad4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ControlVolumen4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label_AjustePAD_5)
                    .addComponent(label_AjustePAD_6))
                .addGap(18, 18, 18)
                .addGroup(panelPrincipal_TresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelPrincipal_TresLayout.createSequentialGroup()
                        .addComponent(botonSelecccionarSonido_Pad6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonConsultarSonido_Pad6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonEliminarSonido_Pad66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ControlVolumen5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipal_TresLayout.createSequentialGroup()
                        .addComponent(botonSelecccionarSonido_Pad5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonConsultarSonido_Pad5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonEliminarSonido_Pad5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ControlVolumen6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bg.add(panelPrincipal_Tres, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 20, 250, 600));

        panelPrincipal_Cuatro.setBackground(new java.awt.Color(18, 18, 18));
        panelPrincipal_Cuatro.setToolTipText("");
        panelPrincipal_Cuatro.setRoundBottomLeft(25);
        panelPrincipal_Cuatro.setRoundBottomRight(25);
        panelPrincipal_Cuatro.setRoundTopLeft(25);
        panelPrincipal_Cuatro.setRoundTopRight(25);

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 204, 204));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Configuración MIDI");

        jLabel2.setFont(new java.awt.Font("Roboto", 3, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(204, 204, 204));
        jLabel2.setText("Puerto COM:");

        comboBoxCOM.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton1.setBackground(new java.awt.Color(153, 255, 153));
        jButton1.setFont(new java.awt.Font("Roboto", 0, 10)); // NOI18N
        jButton1.setForeground(new java.awt.Color(0, 0, 0));
        jButton1.setText("Conectar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 51, 51));
        jButton2.setFont(new java.awt.Font("Roboto Black", 0, 10)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Desconectar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        labelEstado.setFont(new java.awt.Font("Roboto", 3, 12)); // NOI18N
        labelEstado.setForeground(new java.awt.Color(204, 204, 204));
        labelEstado.setText("Estado: Desconectado");

        javax.swing.GroupLayout panelPrincipal_CuatroLayout = new javax.swing.GroupLayout(panelPrincipal_Cuatro);
        panelPrincipal_Cuatro.setLayout(panelPrincipal_CuatroLayout);
        panelPrincipal_CuatroLayout.setHorizontalGroup(
            panelPrincipal_CuatroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipal_CuatroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipal_CuatroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelPrincipal_CuatroLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxCOM, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelPrincipal_CuatroLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelPrincipal_CuatroLayout.setVerticalGroup(
            panelPrincipal_CuatroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipal_CuatroLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelPrincipal_CuatroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(comboBoxCOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelPrincipal_CuatroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelEstado)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        bg.add(panelPrincipal_Cuatro, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, 190, 170));

        panelPrincipal_Cinco.setBackground(new java.awt.Color(18, 18, 18));
        panelPrincipal_Cinco.setRoundBottomLeft(25);
        panelPrincipal_Cinco.setRoundBottomRight(25);
        panelPrincipal_Cinco.setRoundTopLeft(25);
        panelPrincipal_Cinco.setRoundTopRight(25);

        label_TituloCancion.setBackground(new java.awt.Color(210, 210, 210));
        label_TituloCancion.setFont(new java.awt.Font("Roboto", 1, 24)); // NOI18N
        label_TituloCancion.setForeground(new java.awt.Color(210, 210, 210));
        label_TituloCancion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label_TituloCancion.setText("<html>Song Title</html>");

        label_MusicTimeFinal.setBackground(new java.awt.Color(210, 210, 210));
        label_MusicTimeFinal.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        label_MusicTimeFinal.setForeground(new java.awt.Color(230, 230, 230));
        label_MusicTimeFinal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_MusicTimeFinal.setText("0:00");

        label_MusicTimeInicial.setBackground(new java.awt.Color(210, 210, 210));
        label_MusicTimeInicial.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        label_MusicTimeInicial.setForeground(new java.awt.Color(230, 230, 230));
        label_MusicTimeInicial.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_MusicTimeInicial.setText("0:00");

        barraDeProgresoCancion.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                barraDeProgresoCancionMouseDragged(evt);
            }
        });
        barraDeProgresoCancion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                barraDeProgresoCancionMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                barraDeProgresoCancionMousePressed(evt);
            }
        });

        panelReiniciarCancion.setBackground(new java.awt.Color(204, 204, 204));
        panelReiniciarCancion.setRoundBottomLeft(40);
        panelReiniciarCancion.setRoundBottomRight(40);
        panelReiniciarCancion.setRoundTopLeft(40);
        panelReiniciarCancion.setRoundTopRight(40);

        reiniciarCancion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reiniciarCancion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Reiniciar.png"))); // NOI18N
        reiniciarCancion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        reiniciarCancion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reiniciarCancionMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelReiniciarCancionLayout = new javax.swing.GroupLayout(panelReiniciarCancion);
        panelReiniciarCancion.setLayout(panelReiniciarCancionLayout);
        panelReiniciarCancionLayout.setHorizontalGroup(
            panelReiniciarCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelReiniciarCancionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(reiniciarCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelReiniciarCancionLayout.setVerticalGroup(
            panelReiniciarCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelReiniciarCancionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(reiniciarCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelPauseCancion.setBackground(new java.awt.Color(204, 204, 204));
        panelPauseCancion.setRoundBottomLeft(40);
        panelPauseCancion.setRoundBottomRight(40);
        panelPauseCancion.setRoundTopLeft(40);
        panelPauseCancion.setRoundTopRight(40);

        pauseCancion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pauseCancion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Pause.png"))); // NOI18N
        pauseCancion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pauseCancion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pauseCancionMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelPauseCancionLayout = new javax.swing.GroupLayout(panelPauseCancion);
        panelPauseCancion.setLayout(panelPauseCancionLayout);
        panelPauseCancionLayout.setHorizontalGroup(
            panelPauseCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPauseCancionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pauseCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelPauseCancionLayout.setVerticalGroup(
            panelPauseCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPauseCancionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pauseCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelSubirCancion.setBackground(new java.awt.Color(204, 204, 204));
        panelSubirCancion.setRoundBottomLeft(40);
        panelSubirCancion.setRoundBottomRight(40);
        panelSubirCancion.setRoundTopLeft(40);
        panelSubirCancion.setRoundTopRight(40);

        subirCancion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        subirCancion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/SubirArchivo.png"))); // NOI18N
        subirCancion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        subirCancion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                subirCancionMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelSubirCancionLayout = new javax.swing.GroupLayout(panelSubirCancion);
        panelSubirCancion.setLayout(panelSubirCancionLayout);
        panelSubirCancionLayout.setHorizontalGroup(
            panelSubirCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSubirCancionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(subirCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelSubirCancionLayout.setVerticalGroup(
            panelSubirCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSubirCancionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(subirCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelPlayCancion.setBackground(new java.awt.Color(204, 204, 204));
        panelPlayCancion.setRoundBottomLeft(40);
        panelPlayCancion.setRoundBottomRight(40);
        panelPlayCancion.setRoundTopLeft(40);
        panelPlayCancion.setRoundTopRight(40);

        playCancion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        playCancion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Play.png"))); // NOI18N
        playCancion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        playCancion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playCancionMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelPlayCancionLayout = new javax.swing.GroupLayout(panelPlayCancion);
        panelPlayCancion.setLayout(panelPlayCancionLayout);
        panelPlayCancionLayout.setHorizontalGroup(
            panelPlayCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(playCancion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );
        panelPlayCancionLayout.setVerticalGroup(
            panelPlayCancionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(playCancion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelPrincipal_CincoLayout = new javax.swing.GroupLayout(panelPrincipal_Cinco);
        panelPrincipal_Cinco.setLayout(panelPrincipal_CincoLayout);
        panelPrincipal_CincoLayout.setHorizontalGroup(
            panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipal_CincoLayout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(sliderVolumenCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(panelSubirCancion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelPlayCancion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelPauseCancion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelReiniciarCancion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(114, 114, 114))
            .addGroup(panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelPrincipal_CincoLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addGroup(panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label_TituloCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(panelPrincipal_CincoLayout.createSequentialGroup()
                            .addComponent(label_MusicTimeInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(barraDeProgresoCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(label_MusicTimeFinal)))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        panelPrincipal_CincoLayout.setVerticalGroup(
            panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipal_CincoLayout.createSequentialGroup()
                .addContainerGap(124, Short.MAX_VALUE)
                .addGroup(panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(sliderVolumenCancion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelSubirCancion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPlayCancion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPauseCancion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelReiniciarCancion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
            .addGroup(panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelPrincipal_CincoLayout.createSequentialGroup()
                    .addGap(0, 6, Short.MAX_VALUE)
                    .addComponent(label_TituloCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(panelPrincipal_CincoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label_MusicTimeInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(panelPrincipal_CincoLayout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addComponent(barraDeProgresoCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(label_MusicTimeFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(0, 62, Short.MAX_VALUE)))
        );

        bg.add(panelPrincipal_Cinco, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 450, 410, 170));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.DEFAULT_SIZE, 949, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Boton guardar Pads
    private void label_GuardarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_GuardarMouseClicked
        saveAudioFiles();
    }//GEN-LAST:event_label_GuardarMouseClicked

    //Boton reestablecer todos los pads
    private void label_ReestablecerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_ReestablecerMouseClicked
        resetAudioFiles();
    }//GEN-LAST:event_label_ReestablecerMouseClicked

    private void label_GuardarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_GuardarMouseEntered
        botonGuardar.setBackground(new Color(180, 180, 180));
    }//GEN-LAST:event_label_GuardarMouseEntered

    private void label_GuardarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_GuardarMouseExited
        botonGuardar.setBackground(new Color(204, 204, 204));
    }//GEN-LAST:event_label_GuardarMouseExited

    private void label_ReestablecerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_ReestablecerMouseEntered
        botonReestablecer.setBackground(new Color(180, 180, 180));
    }//GEN-LAST:event_label_ReestablecerMouseEntered

    private void label_ReestablecerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_ReestablecerMouseExited
        botonReestablecer.setBackground(new Color(204, 204, 204));
    }//GEN-LAST:event_label_ReestablecerMouseExited

    private void subirCancionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_subirCancionMouseClicked
        musicPlayer.subirCancion();
    }//GEN-LAST:event_subirCancionMouseClicked

    private void pauseCancionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseCancionMouseClicked
        musicPlayer.pauseMusic();
    }//GEN-LAST:event_pauseCancionMouseClicked

    private void reiniciarCancionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reiniciarCancionMouseClicked
        musicPlayer.reiniciarCancion();
    }//GEN-LAST:event_reiniciarCancionMouseClicked

    private void playCancionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playCancionMouseClicked
        musicPlayer.playMusic();
    }//GEN-LAST:event_playCancionMouseClicked

    private void barraDeProgresoCancionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_barraDeProgresoCancionMouseClicked
        // Obtener la posición del clic y el ancho de la barra
        int clickPosition = evt.getX();
        int barraWidth = barraDeProgresoCancion.getWidth();

        // Calcular la nueva posición en segundos
        long newPosition = (long) ((clickPosition / (double) barraWidth) * musicPlayer.getMusicLength() / 1_000_000);

        // Mover la posición de la canción
        musicPlayer.setMusicPosition(newPosition);

        // Actualizar visualmente el tiempo y la barra
        barraDeProgresoCancion.setValue((int) newPosition);
        label_MusicTimeInicial.setText(formatTime(newPosition)); // Formatear tiempo en minutos:segundos
    }//GEN-LAST:event_barraDeProgresoCancionMouseClicked

    private void barraDeProgresoCancionMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_barraDeProgresoCancionMouseDragged
        int dragPosition = evt.getX(); // Obtener la posición del mouse en la barra
        int barraWidth = barraDeProgresoCancion.getWidth(); // Ancho de la barra
        long newPosition = (long) ((dragPosition / (double) barraWidth) * musicPlayer.getMusicLength());

        // Ajustar la barra y la posición del tiempo
        barraDeProgresoCancion.setValue((int) (newPosition / 1_000_000)); // Actualiza visualmente la barra
        musicPlayer.setMusicPosition(newPosition / 1_000_000); // Actualiza la posición de la música

        // Mostrar el tiempo actualizado en el label
        label_MusicTimeInicial.setText(formatTime(newPosition / 1_000_000));
    }//GEN-LAST:event_barraDeProgresoCancionMouseDragged

    private void barraDeProgresoCancionMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_barraDeProgresoCancionMousePressed
        musicPlayer.pauseMusic(); // Pausa la música al empezar a arrastrar
    }//GEN-LAST:event_barraDeProgresoCancionMousePressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        conectarPuerto();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        desconectarPuerto(puerto);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void consultarInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_consultarInfoMouseClicked
        // Mostramos el JOptionPane con la información
        JOptionPane.showMessageDialog(
                null,
                "Los pads no tienen sonidos asignados por defecto. Puedes usar los sonidos disponibles en la carpeta junto al ejecutable.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_consultarInfoMouseClicked

    // Método auxiliar para formatear el tiempo
    private String formatTime(long seconds) {
        return String.format("%d:%02d", TimeUnit.SECONDS.toMinutes(seconds), seconds % 60);
    }

    public void cambiarVentana(JFrame nuevaVentana) {
        nuevaVentana.setVisible(true);
        nuevaVentana.setLocationRelativeTo(null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PrincipalSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PrincipalSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PrincipalSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PrincipalSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new PrincipalSampler().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider ControlVolumen1;
    private javax.swing.JSlider ControlVolumen2;
    private javax.swing.JSlider ControlVolumen3;
    private javax.swing.JSlider ControlVolumen4;
    private javax.swing.JSlider ControlVolumen5;
    private javax.swing.JSlider ControlVolumen6;
    private javax.swing.JProgressBar barraDeProgresoCancion;
    private javax.swing.JPanel bg;
    private interfaceSampler.PanelRound botonConsultarSonido_Pad1;
    private interfaceSampler.PanelRound botonConsultarSonido_Pad2;
    private interfaceSampler.PanelRound botonConsultarSonido_Pad3;
    private interfaceSampler.PanelRound botonConsultarSonido_Pad44;
    private interfaceSampler.PanelRound botonConsultarSonido_Pad5;
    private interfaceSampler.PanelRound botonConsultarSonido_Pad6;
    private interfaceSampler.PanelRound botonEliminarSonido_Pad1;
    private interfaceSampler.PanelRound botonEliminarSonido_Pad2;
    private interfaceSampler.PanelRound botonEliminarSonido_Pad3;
    private interfaceSampler.PanelRound botonEliminarSonido_Pad4;
    private interfaceSampler.PanelRound botonEliminarSonido_Pad5;
    private interfaceSampler.PanelRound botonEliminarSonido_Pad66;
    private interfaceSampler.PanelRound botonGuardar;
    private interfaceSampler.PanelRound botonReestablecer;
    private interfaceSampler.PanelRound botonSelecccionarSonido_Pad1;
    private interfaceSampler.PanelRound botonSelecccionarSonido_Pad2;
    private interfaceSampler.PanelRound botonSelecccionarSonido_Pad3;
    private interfaceSampler.PanelRound botonSelecccionarSonido_Pad4;
    private interfaceSampler.PanelRound botonSelecccionarSonido_Pad5;
    private interfaceSampler.PanelRound botonSelecccionarSonido_Pad6;
    private javax.swing.JComboBox<String> comboBoxCOM;
    private javax.swing.JLabel consultarInfo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelEstado;
    private javax.swing.JLabel labelPadSound1;
    private javax.swing.JLabel labelPadSound2;
    private javax.swing.JLabel labelPadSound3;
    private javax.swing.JLabel labelPadSound4;
    private javax.swing.JLabel labelPadSound5;
    private javax.swing.JLabel labelPadSound6;
    private javax.swing.JLabel labelPerfil_Seleccionado;
    private javax.swing.JLabel labelTitulo_PADS;
    private javax.swing.JLabel labelTitulo_PADS1;
    private javax.swing.JLabel label_AjustePAD_1;
    private javax.swing.JLabel label_AjustePAD_2;
    private javax.swing.JLabel label_AjustePAD_3;
    private javax.swing.JLabel label_AjustePAD_4;
    private javax.swing.JLabel label_AjustePAD_5;
    private javax.swing.JLabel label_AjustePAD_6;
    private javax.swing.JLabel label_ConsultarSonido1;
    private javax.swing.JLabel label_ConsultarSonido2;
    private javax.swing.JLabel label_ConsultarSonido3;
    private javax.swing.JLabel label_ConsultarSonido4;
    private javax.swing.JLabel label_ConsultarSonido5;
    private javax.swing.JLabel label_ConsultarSonido6;
    private javax.swing.JLabel label_EliminarSonido1;
    private javax.swing.JLabel label_EliminarSonido2;
    private javax.swing.JLabel label_EliminarSonido3;
    private javax.swing.JLabel label_EliminarSonido4;
    private javax.swing.JLabel label_EliminarSonido5;
    private javax.swing.JLabel label_EliminarSonido6;
    private javax.swing.JLabel label_Guardar;
    private javax.swing.JLabel label_ImageLibrary;
    private javax.swing.JLabel label_MusicTimeFinal;
    private javax.swing.JLabel label_MusicTimeInicial;
    private javax.swing.JLabel label_NumeroDelPAD_1;
    private javax.swing.JLabel label_NumeroDelPAD_2;
    private javax.swing.JLabel label_NumeroDelPAD_3;
    private javax.swing.JLabel label_NumeroDelPAD_4;
    private javax.swing.JLabel label_NumeroDelPAD_5;
    private javax.swing.JLabel label_NumeroDelPAD_6;
    private javax.swing.JLabel label_PerfilSeleccionado;
    private javax.swing.JLabel label_Reestablecer;
    private javax.swing.JLabel label_SeleccionarSonido1;
    private javax.swing.JLabel label_SeleccionarSonido2;
    private javax.swing.JLabel label_SeleccionarSonido3;
    private javax.swing.JLabel label_SeleccionarSonido4;
    private javax.swing.JLabel label_SeleccionarSonido5;
    private javax.swing.JLabel label_SeleccionarSonido6;
    private javax.swing.JLabel label_TextoGuardar;
    private javax.swing.JLabel label_TextoReestablecer;
    private javax.swing.JLabel label_TituloCancion;
    private javax.swing.JLabel label_TituloGuardar;
    private javax.swing.JLabel label_TituloReestablecer;
    private interfaceSampler.PanelRound padSound1;
    private interfaceSampler.PanelRound padSound2;
    private interfaceSampler.PanelRound padSound3;
    private interfaceSampler.PanelRound padSound4;
    private interfaceSampler.PanelRound padSound5;
    private interfaceSampler.PanelRound padSound6;
    private interfaceSampler.PanelRound panelConsultarInfo;
    private interfaceSampler.PanelRound panelGuardar;
    private interfaceSampler.PanelRound panelHeaderPads;
    private interfaceSampler.PanelRound panelHeaderPads1;
    private interfaceSampler.PanelRound panelPauseCancion;
    private interfaceSampler.PanelRound panelPerfil;
    private interfaceSampler.PanelRound panelPlayCancion;
    private interfaceSampler.PanelRound panelPrincipal_Cinco;
    private interfaceSampler.PanelRound panelPrincipal_Cuatro;
    private interfaceSampler.PanelRound panelPrincipal_Dos;
    private javax.swing.JPanel panelPrincipal_Tres;
    private interfaceSampler.PanelRound panelPrincipal_Uno;
    private interfaceSampler.PanelRound panelReestablecer;
    private interfaceSampler.PanelRound panelReiniciarCancion;
    private interfaceSampler.PanelRound panelSubirCancion;
    private interfaceSampler.PanelRound panelTextoGuardar;
    private interfaceSampler.PanelRound panelTextoReestablecer;
    private javax.swing.JLabel pauseCancion;
    private javax.swing.JLabel playCancion;
    private javax.swing.JLabel reiniciarCancion;
    private javax.swing.JSlider sliderVolumenCancion;
    private javax.swing.JLabel subirCancion;
    // End of variables declaration//GEN-END:variables
}
