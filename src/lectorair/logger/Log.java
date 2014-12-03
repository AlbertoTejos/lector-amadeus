
package lectorair.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import lectorair.MetodosStaticos;
import lectorair.lectorarchivo.Archivo;
import lectorair.lectorarchivo.Ticket;


public class Log {
    private File archivo;
    private String nombre;
    private String fecha;
    private static Log instance= null;

    
   public static Log getInstance(){
        if (instance==null) {
           instance = new Log();
        }
        
        if (!instance.fecha.equals(MetodosStaticos.getFecha())) {
           instance = new Log();
        }
        return instance;
   }
   
   
  
   
//   private File getArchivo(){
//       
//       return null;
//   }
   
   
   
   private Log(){
       this.fecha = MetodosStaticos.getFecha();
       this.nombre = "log_"+this.fecha+".oris";
       
       archivo = new File("log/"+nombre);
       new File("log/").mkdirs();
       if (!archivo.exists()) {
           try {
               archivo.createNewFile();
           } catch (IOException ex) {
               System.out.println(ex);
           }
       }
   } 
    
   
   public void agregarLog(Archivo arc, File F,boolean exito){       
       try {
             
             FileWriter w = new FileWriter(archivo,true);
             BufferedWriter bw = new BufferedWriter(w);
             PrintWriter wr = new PrintWriter(bw);
             if (arc!=null) {
                wr.println("ARCHIVO : "+arc.getArchivo().getName()); 
                wr.println("PROCESADO : "+MetodosStaticos.getHora());
                for (Ticket tik : arc.getPajaseros()) {
                    wr.println("TICKET "+tik.getPosicion()+" : "+tik.getTicket()+arc.getEstado());
                }
             }else{
                 wr.println("ARCHIVO : "+F.getName());
                 wr.println("PROCESADO : "+MetodosStaticos.getHora());
                 
             }
             
             if (exito) {
                 wr.println("ARCHIVO ACEPTADO");
             }else{
                 wr.println("ARCHIVO RECHAZADO");
             }
             
             wr.println(" ");
             wr.close();
             bw.close();
             w.close();

        } catch (IOException ex) {
            System.out.println(ex);
        }
            
   }

//    public static void main(String[] args) {
//        Log a = Log.getInstance();
//        a.getArchivo();
//    }
        
}
