package lectorair;

import java.util.Calendar;
import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class MetodosStaticos {
    
public static String abrirArchivo(JFrame frame) {

   /**llamamos el metodo que permite cargar la ventana*/
   JFileChooser file=new JFileChooser();
   file.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
   int result = file.showOpenDialog(frame);
   
   switch(result){
       case JFileChooser.CANCEL_OPTION :
           return "";

       case JFileChooser.APPROVE_OPTION:
           return file.getSelectedFile().getAbsolutePath();
       
       case JFileChooser.ERROR_OPTION:
           return "";   
   }
   return "";
}

 public  static String getFecha(){
       Calendar c1 = Calendar.getInstance();
       String dia = Integer.toString(c1.get(Calendar.DATE));
       String mes = Integer.toString(c1.get(Calendar.MONTH)+1);
       String annio = Integer.toString(c1.get(Calendar.YEAR));
       return annio.substring(2)+getNumeroZero(dia)+getNumeroZero(mes);
   }
   
   
 public static String getHora(){
     Calendar calendario = Calendar.getInstance();
       int hora, minutos, segundos;
       hora =calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);
       return hora + ":" + minutos + ":" + segundos;
 }
 
 
   public static String getNumeroZero(String numero){
       int num = Integer.parseInt(numero);
       if (num < 10) {
           return "0"+num;
       }else{
           return ""+num;
       }
   }


}
    

