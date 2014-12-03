
package lectorair.datos.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

public class Parametros {

    private BufferedReader bf;
    private String ruta_lectura;
    private String ruta_leidos;
    private String ruta_error;
    private String servidor;
    private String usuario;
    private String contrasena;
    private int tiempo_conexion;
    private String segmento_numfile;
    private static Parametros instance= null;

    
   public static Parametros getInstance(){
        if (instance==null) {
           instance = new Parametros();
        }
        return instance;
   }

    public BufferedReader getBf() {
        return bf;
    }

    public void setBf(BufferedReader bf) {
        this.bf = bf;
    }

    public String getSegmento_numfile() {
        return segmento_numfile;
    }

    public void setSegmento_numfile(String segmento_numfile) {
        this.segmento_numfile = segmento_numfile;
    }
   
   
   
    public String getRuta_lectura() {
        return ruta_lectura;
    }

    public void setRuta_lectura(String ruta_lectura) {
        this.ruta_lectura = ruta_lectura;
    }

    public String getRuta_leidos() {
        return ruta_leidos;
    }

    public void setRuta_leidos(String ruta_leidos) {
        this.ruta_leidos = ruta_leidos;
    }

    public String getRuta_error() {
        return ruta_error;
    }

    public void setRuta_error(String ruta_error) {
        this.ruta_error = ruta_error;
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public int getTiempo_conexion() {
        return tiempo_conexion;
    }

    public void setTiempo_conexion(int tiempo_conexion) {
        this.tiempo_conexion = tiempo_conexion;
    }
    
    
    private Parametros(){
        
        try {
            bf = new BufferedReader(new FileReader("parametros.conf"));
            String cadena;
            while ((cadena = bf.readLine())!=null) {
                if (!cadena.equals("")) {
                    String[] split = cadena.split("=>");
                    if (split.length<=1) {
                        split = new String[]{split[0],""};
                    }
                    switch (split[0].trim()) {
                        case "ruta_lectura":
                            this.ruta_lectura=split[1].trim();
                            break;
                        case "ruta_leidos":
                            this.ruta_leidos=split[1].trim();
                            break;
                        case "ruta_error":
                            this.ruta_error=split[1].trim();
                            break;
                        case "servidor":
                            this.servidor=split[1].trim();
                            break;
                        case "usuario":
                            this.usuario=split[1].trim();              
                            break;
                        case "contrasena":
                            this.contrasena=split[1].trim();
                            break;
                        case "segmento_numfile":
                            this.segmento_numfile=split[1].trim();
                            break;    
                        case "tiempo_conexion":
                            if (split[1]!=null && !split[1].equals("")) {
                                this.tiempo_conexion=Integer.parseInt(split[1].trim());
                            }else{
                                this.tiempo_conexion=10;
                            }

                            break;
                    }
                }
            }
            bf.close();
        }catch(IOException ioe) {
            JOptionPane.showMessageDialog(null, "No se ha encontrado el archivo de configuracion.");
        }
        
        if (tiempo_conexion < 1) {
            tiempo_conexion = 1;
        }
    }
    
    
    
    public boolean guardarCambios(){
        File f;
        f = new File("parametros.conf");
        try{
            FileWriter w = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(w);
            PrintWriter wr = new PrintWriter(bw);
            wr.println("ruta_lectura=>"+this.ruta_lectura); 
            wr.println("ruta_leidos=>"+this.ruta_leidos);
            wr.println("ruta_error=>"+this.ruta_error);
            wr.println("servidor=>"+this.servidor);
            wr.println("usuario=>"+this.usuario);
            wr.println("contrasena=>"+this.contrasena);
            wr.println("tiempo_conexion=>"+this.tiempo_conexion);
            wr.println("segmento_numfile=>"+this.segmento_numfile);
            wr.close();
            bw.close();
            JOptionPane.showMessageDialog(null,"Se ha guardado la configuración correctamente.");
            return true;
        }catch(IOException e){
            
            JOptionPane.showMessageDialog(null,"No se pudo guardar la configuración, intente nuevamente.");
            return false;
        }
 
    }
        
   }
    
    
    
    
    
