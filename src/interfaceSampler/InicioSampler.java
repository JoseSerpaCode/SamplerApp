package interfaceSampler;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * @author José_Serpa
 */
public class InicioSampler extends javax.swing.JFrame {

    int xMouse, yMouse;

    public InicioSampler() {
        initComponents();
        this.setLocationRelativeTo(null);
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/padland.jpg"));
        setIconImage(icon.getImage());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JPanel();
        panelCerrar = new interfaceSampler.PanelRound();
        labelCerrar = new javax.swing.JLabel();
        panelIniciar = new interfaceSampler.PanelRound();
        labelIniciar = new javax.swing.JLabel();
        labelTitulo_PadLand = new javax.swing.JLabel();
        labelImagenMenu = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PadLand");
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bg.setBackground(new java.awt.Color(0, 0, 0));
        bg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelCerrar.setBackground(new java.awt.Color(33, 33, 33));
        panelCerrar.setPreferredSize(new java.awt.Dimension(171, 47));
        panelCerrar.setRoundBottomLeft(15);
        panelCerrar.setRoundBottomRight(15);
        panelCerrar.setRoundTopLeft(15);
        panelCerrar.setRoundTopRight(15);

        labelCerrar.setFont(new java.awt.Font("Roboto", 1, 22)); // NOI18N
        labelCerrar.setForeground(new java.awt.Color(255, 255, 255));
        labelCerrar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCerrar.setText("Cerrar");
        labelCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelCerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelCerrarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                labelCerrarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                labelCerrarMouseExited(evt);
            }
        });

        javax.swing.GroupLayout panelCerrarLayout = new javax.swing.GroupLayout(panelCerrar);
        panelCerrar.setLayout(panelCerrarLayout);
        panelCerrarLayout.setHorizontalGroup(
            panelCerrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 171, Short.MAX_VALUE)
            .addGroup(panelCerrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelCerrarLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(labelCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        panelCerrarLayout.setVerticalGroup(
            panelCerrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 47, Short.MAX_VALUE)
            .addGroup(panelCerrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelCerrarLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(labelCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        bg.add(panelCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 440, 171, 47));

        panelIniciar.setPreferredSize(new java.awt.Dimension(171, 47));
        panelIniciar.setRoundBottomLeft(15);
        panelIniciar.setRoundBottomRight(15);
        panelIniciar.setRoundTopLeft(15);
        panelIniciar.setRoundTopRight(15);

        labelIniciar.setFont(new java.awt.Font("Roboto", 1, 22)); // NOI18N
        labelIniciar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelIniciar.setText("Iniciar");
        labelIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelIniciar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelIniciarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                labelIniciarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                labelIniciarMouseExited(evt);
            }
        });

        javax.swing.GroupLayout panelIniciarLayout = new javax.swing.GroupLayout(panelIniciar);
        panelIniciar.setLayout(panelIniciarLayout);
        panelIniciarLayout.setHorizontalGroup(
            panelIniciarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 171, Short.MAX_VALUE)
            .addGroup(panelIniciarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelIniciarLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(labelIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        panelIniciarLayout.setVerticalGroup(
            panelIniciarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 47, Short.MAX_VALUE)
            .addGroup(panelIniciarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelIniciarLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(labelIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        bg.add(panelIniciar, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 440, -1, -1));

        labelTitulo_PadLand.setFont(new java.awt.Font("Jaini", 0, 48)); // NOI18N
        labelTitulo_PadLand.setForeground(new java.awt.Color(255, 255, 255));
        labelTitulo_PadLand.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelTitulo_PadLand.setText("PadLand");
        bg.add(labelTitulo_PadLand, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 330, 200, 70));

        labelImagenMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/menuInicio.png"))); // NOI18N
        labelImagenMenu.setText("jLabel1");
        labelImagenMenu.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                labelImagenMenuMouseDragged(evt);
            }
        });
        labelImagenMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                labelImagenMenuMousePressed(evt);
            }
        });
        bg.add(labelImagenMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 560, 540));

        getContentPane().add(bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 560, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*
    
    Eventos de los BOTONES
    
     */
    //Evento Boton Cerrar
    private void labelCerrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCerrarMouseClicked
        System.exit(0);
    }//GEN-LAST:event_labelCerrarMouseClicked

    //Evento Boton Iniciar
    private void labelIniciarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelIniciarMouseClicked
        cambiarVentana(new PrincipalSampler());
    }//GEN-LAST:event_labelIniciarMouseClicked

    /*
    
    Elementos de Decoración
    
     */
    //Cuando el mouse está sobre el labelCerrar/panelCerrar
    private void labelCerrarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCerrarMouseEntered
        panelCerrar.setBackground(new Color(235, 30, 50));
        labelCerrar.setForeground(Color.WHITE);
    }//GEN-LAST:event_labelCerrarMouseEntered

    //Cuando el mouse NO se encuentra sobre el labelCerrar/panelCerrar
    private void labelCerrarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCerrarMouseExited
        panelCerrar.setBackground(new Color(33, 33, 33));
    }//GEN-LAST:event_labelCerrarMouseExited

    //Cuando el mouse está sobre el labelIniciar/panelIniciar
    private void labelIniciarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelIniciarMouseEntered
        panelIniciar.setBackground(new Color(119, 55, 149));
        labelIniciar.setForeground(Color.WHITE);
    }//GEN-LAST:event_labelIniciarMouseEntered

    //Cuando el mouse NO se encuentra sobre el labelIniciar/panelIniciar
    private void labelIniciarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelIniciarMouseExited
        panelIniciar.setBackground(Color.WHITE);
        labelIniciar.setForeground(Color.BLACK);
    }//GEN-LAST:event_labelIniciarMouseExited

    /*
    
    Evento para mover la ventana
    
     */
    //Obtención de la posición del mouse
    private void labelImagenMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelImagenMenuMousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_labelImagenMenuMousePressed

    //Mover la ventana con los valores del mouse y la posición de la ventana
    private void labelImagenMenuMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelImagenMenuMouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xMouse, y - yMouse);
    }//GEN-LAST:event_labelImagenMenuMouseDragged

    public void cambiarVentana(JFrame nuevaVentana) {
        nuevaVentana.setVisible(true);
        nuevaVentana.setLocationRelativeTo(null);
        this.setVisible(false);
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
            java.util.logging.Logger.getLogger(InicioSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InicioSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InicioSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InicioSampler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InicioSampler().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bg;
    private javax.swing.JLabel labelCerrar;
    private javax.swing.JLabel labelImagenMenu;
    private javax.swing.JLabel labelIniciar;
    private javax.swing.JLabel labelTitulo_PadLand;
    private interfaceSampler.PanelRound panelCerrar;
    private interfaceSampler.PanelRound panelIniciar;
    // End of variables declaration//GEN-END:variables
}
