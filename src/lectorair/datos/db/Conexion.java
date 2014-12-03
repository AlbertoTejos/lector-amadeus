
package lectorair.datos.db;

import java.sql.SQLException;
import javax.swing.JOptionPane;


public class Conexion {

    private java.sql.Connection con = null;
    private String url;
    private String userName ;
    private String password ;
    private Parametros par;
    // Indica al controlador que debe utilizar un cursor de servidor, // lo que permite más de una instrucción activa // en una conexión.
    private final String selectMethod = "cursor";

    public Conexion() {
        par = Parametros.getInstance();
        this.url = par.getServidor();
        this.userName = par.getUsuario();
        this.password = par.getContrasena();
    }

    
    
  
    
    private String getConnectionUrl() {
        return url + /*serverName + ":" + portNumber + ";databaseName=" + databaseName +*/ ";selectMethod=" + selectMethod + ";";
    }

    protected java.sql.Connection getConnection() {
        try {
           Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = java.sql.DriverManager.getConnection(getConnectionUrl(), userName, password);
            if (con != null) {
                System.out.println("Conexión correcta.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
            System.out.println("Error de seguimiento en getConnection() : " + e.getMessage());
        }
        return con; 
    }
    
    
    public static boolean probarConexion(String url,String user , String pass){
        java.sql.Connection con = null;      
        try {
           Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = java.sql.DriverManager.getConnection(url, user, pass);
            if (con != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    public static void main(String[] args){
        
        Conexion conn = new Conexion();
        //Información del servidor y controlador
        conn.displayDbProperties();
        System.out.println("URL DE CONEXION: "+conn.getConnectionUrl());
    }

    
    /* Mostrar las propiedades del controlador y los detalles de la base de datos */
    public void displayDbProperties() {
        java.sql.DatabaseMetaData dm = null;
        java.sql.ResultSet rs = null;
        try {
            con = this.getConnection();
            if (con != null) {
                dm = con.getMetaData();
                System.out.println("Información del controlador");
                System.out.println("\tNombre del controlador: " + dm.getDriverName());
                System.out.println("\tVersión del controlador: " + dm.getDriverVersion());
                System.out.println("\nInformación de la base de datos ");
                System.out.println("\tNombre de la base de datos: " + dm.getDatabaseProductName());
                System.out.println("\tVersión de la base de datos: " + dm.getDatabaseProductVersion());
                System.out.println("\nCatálogos disponibles ");
                rs = dm.getCatalogs();
                while (rs.next()) {
                    System.out.println("\tcatálogo: " + rs.getString(1));
                }
                rs.close();
                rs = null;
                closeConnection();
            } else {
                System.out.println("Error: No hay ninguna conexión activa");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dm = null;
    }

    private void closeConnection() {
        try {
            if (con != null) {
                con.close();
            }
            con = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    


}

    
