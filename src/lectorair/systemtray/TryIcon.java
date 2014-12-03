package lectorair.systemtray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import lectorair.datos.db.ArchivoDAO;
import lectorair.datos.db.Parametros;
import lectorair.directorios.Directorio;
import lectorair.lectorarchivo.Archivo;
import lectorair.logger.Log;

public class TryIcon {

    private Image image = new ImageIcon(getClass().getResource("avion.png")).getImage();
    private final TrayIcon trayIcon = new TrayIcon(image, "LectorOris");
    private Timer timer;
    private boolean band;
    private Parametros param;

    public TryIcon() {
        if (SystemTray.isSupported()) {
            SystemTray systemtray = SystemTray.getSystemTray();
            MyMouseListener mouseListener = new MyMouseListener();
            trayIcon.setPopupMenu(mouseListener.getPopup());
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(mouseListener);

            try {
                systemtray.add(trayIcon);
                MensajeTrayIcon("La aplicacion se ha iniciado correctamente", MessageType.INFO);
                System.out.println("La aplicacion se ha iniciado correctamente");
            } catch (AWTException e) {
                System.err.println("Error:" + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "El sistema no soporta este tipo de aplicaciones.");
        }

        param = Parametros.getInstance();
        timer = new Timer();
        timer.schedule(new TareaBackGraund(), 0, ((param.getTiempo_conexion() * 60) * 1000));//Se ejecuta cada 10 segundos

    }

    //Muestra una burbuja con la accion que se realiza
    public void MensajeTrayIcon(String texto, MessageType tipo) {
        trayIcon.displayMessage("Lector TKT : ", texto, tipo);
    }

    //Clase interna que manejara una accion en segundo plano
    class TareaBackGraund extends TimerTask {

        int cont = 0;

        @Override
        public void run() {
            if (band) {
                timer.cancel();
            } else {
                tareaBackgraund();
            }
        }

        public void tareaBackgraund() {
            System.out.println("Buscando archivos");
            Directorio a = new Directorio(param.getRuta_lectura(), param.getRuta_leidos(), param.getRuta_error());
            int errores = 0;
            int leidos = 0;
            int cantidad = 0;
            int rezagados = 0;
            if (a.hayNuevos()) {
                ArchivoDAO adao = new ArchivoDAO();
                File[] files = a.getNuevosFicheros();
                cantidad = files.length;
                System.out.println(cantidad);
                for (File file : files) {
                    Archivo lc = null;
                    boolean exito = false;
                    try {
                        lc = new Archivo(file);
                        System.out.println(lc);
                        adao.insertArchivo(lc);
                        rezagados = adao.getRezagados();
                        if (rezagados > 0) {

                            ArrayList<Archivo> archivosRezagados = adao.archivosRezagados();
                            //Hay 1 archivo en cola de espera
                            //Anulando al ticket 18488545xxx...
                            for (Archivo archivosRezagado : archivosRezagados) {
                                
                                try {
                                    adao.insertArchivo(archivosRezagado);
                                    a.moverLeidos(file); 
                                    leidos++;
                                    exito = true;
                                } catch (SQLException | IOException e) { 
                                    System.out.println(e.getMessage());
                                }
                                
                            }
                        } else {
                            a.moverLeidos(file);
                            leidos++;
                            exito = true; 
                        }

                    } catch (IOException | SQLException ex) {
                        System.out.println("Error IO : " + file.getAbsolutePath() + "\n" + ex);
                        errores++;
                        try {
                            a.moverErrores(file);
                        } catch (IOException ex1) {
                            Logger.getLogger(TryIcon.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                        exito = false;
                    }

                    Log logger = Log.getInstance();
                    logger.agregarLog(lc, file, exito);

                }

                mensajeFinal(cantidad, leidos, errores); 

            }

        }

        public void mensajeFinal(int cnt, int lei, int err) {

            MensajeTrayIcon("Se han encontrado " + cnt + " archivos :\n"
                    + "  " + lei + " Correctos\n"
                    + "  " + err + " Errores", MessageType.INFO);
        }

    }

}
