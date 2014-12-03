
package lectorair.directorios;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import javax.swing.JOptionPane;

public class Directorio {
    private String ruta_lectura;
    private String ruta_leidos;
    private String ruta_errores;

    public Directorio(String ruta_lectura, String ruta_leidos, String ruta_errores) {
        this.ruta_lectura = ruta_lectura;
        this.ruta_leidos = ruta_leidos;
        this.ruta_errores = ruta_errores;
    }
    
    
    public boolean hayNuevos(){
        File f = new File(ruta_lectura);
        if (f.exists()) {
            File[] ficheros = f.listFiles();
            if (ficheros.length>0) {
                return true;
            }
        }else{
            JOptionPane.showMessageDialog(null, "El directorio de lectura no existe.");
            return false;
        }
        return false;
    }

    public File[] getNuevosFicheros(){
        File f = new File(ruta_lectura);
        if (f.exists()) {
            File[] ficheros = f.listFiles();
            if (ficheros.length>0) {
                return ficheros;
            }     
        }else{
            JOptionPane.showMessageDialog(null, "El directorio de lectura no existe.");
            return null;
        }
        return null;
    }
    
    
    @SuppressWarnings("empty-statement")
    public void moverLeidos(File original) throws IOException{
        File destFile = new File(this.ruta_leidos+"\\"+original.getName());
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel origen = null;
        FileChannel destino = null;
        
        try {
            origen = new FileInputStream(original).getChannel();
            destino = new FileOutputStream(destFile).getChannel();

            long count = 0;
            long size = origen.size();              
            while((count += destino.transferFrom(origen, count, size-count))<size);
            
            
        }
        finally {
            if(origen != null) {
                origen.close();
            }
            if(destino != null) {
                destino.close();
            }
        }
        if(destFile.exists()) {
            original.delete();
        }
        
        if (original.exists()) {
            original.deleteOnExit();
        }

        
    }
    
    @SuppressWarnings("empty-statement")
    public void moverErrores(File original) throws IOException{
        File destFile = new File(this.ruta_errores+"\\"+original.getName());
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel origen = null;
        FileChannel destino = null;
        try {
            origen = new FileInputStream(original).getChannel();
            destino = new FileOutputStream(destFile).getChannel();

            long count = 0;
            long size = origen.size();              
            while((count += destino.transferFrom(origen, count, size-count))<size);
            
            
        }
        finally {
            if(origen != null) {
                origen.close();
            }
            if(destino != null) {
                destino.close();
            }
        }
        if(destFile.exists()) {
            original.delete();
        }
    }
    
    
}
