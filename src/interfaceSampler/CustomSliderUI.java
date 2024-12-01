package interfaceSampler;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class CustomSliderUI extends BasicSliderUI {

    private final int thumbWidth = 15;  // Ancho visual del thumb
    private final int thumbHeight = 25; // Altura visual del thumb

    public CustomSliderUI(JSlider slider) {
        super(slider);
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibuja el thumb visual en las coordenadas calculadas del rectángulo del thumb
        g2d.fillRoundRect(thumbRect.x, thumbRect.y, thumbWidth, thumbHeight, 5, 5);
        
        g2d.dispose();
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Color de la pista (línea negra) y ajuste de grosor
        g2d.setColor(Color.BLACK);
        int trackWidth = 5; // Grosor de la pista
        int x = trackRect.x + (trackRect.width - trackWidth) / 2;
        
        // Dibuja una línea recta vertical en el centro del slider
        g2d.fillRoundRect(x, trackRect.y, trackWidth, trackRect.height, 5, 5);
        
        g2d.dispose();
    }
    
    @Override
    protected Dimension getThumbSize() {
        // Define el tamaño del área del thumb, coincide con el tamaño visual
        return new Dimension(thumbWidth, thumbHeight);
    }

    @Override
    public void calculateThumbLocation() {
        super.calculateThumbLocation();
        
        // Ajusta la posición del thumb para que esté centrado visualmente en la pista
        thumbRect.x = thumbRect.x - (thumbWidth - thumbRect.width) / 2;
        thumbRect.y = thumbRect.y - (thumbHeight - thumbRect.height) / 2;
    }

    @Override
    public void setThumbLocation(int x, int y) {
        super.setThumbLocation(x, y);
        slider.repaint();
    }
}
