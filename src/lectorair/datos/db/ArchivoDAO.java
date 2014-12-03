package lectorair.datos.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import lectorair.lectorarchivo.Archivo;
import lectorair.lectorarchivo.Segmento;
import lectorair.lectorarchivo.Ticket;

public class ArchivoDAO extends Conexion {

    private final String campos_ticket = "pnr,num_file,ticket,old_ticket,cod_emd,fecha_emision,fecha_anula,fecha_remision,posicion_pasajero,nombre_pasajero,tipo_pasajero,cod_linea_aerea,ruta,moneda,valor_neto,valor_tasas,valor_final,comision,forma_pago,tipo,estado,gds";
    private final String campos_segmentos = "ticket,nro_segmento,cod_salida,cod_llegada,nom_salida,nom_llegada,fecha_salida,hora_salida,fecha_llegada,hora_llegada,nro_vuelo,linea_aerea,cod_clase";
    private final Connection con = getConnection();
    private ArrayList<Archivo> ticketsEnEspera = new ArrayList<>();
    private static int contador = 0;

    public ArchivoDAO() {
        super();
    }

    public boolean existeTicket(Ticket tic) {

        try {
            ResultSet rs = con.createStatement().executeQuery("SELECT ticket FROM ticket WHERE ticket ='" + tic.getTicket() + "'");
            if (rs.next()) {
                System.out.println("Existe el ticket");
                return true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ArchivoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("No existe el ticket");
        return false;
    }

    public boolean estaAnulando(Archivo arc) {
        return arc.getEstado().equalsIgnoreCase("ANULADO");
    }

    public int getRezagados() {
        return contador;

    }

    public ArrayList<Archivo> archivosRezagados() {

        return ticketsEnEspera;

    }

    public void insertArchivo(Archivo arc) throws SQLException {

        con.createStatement().executeUpdate("BEGIN TRAN;");
        for (Ticket tic : arc.getPajaseros()) {
            if (estaAnulando(arc)) {

                if (!existeTicket(tic)) {
                    System.out.println("No existe, lo almacenamos para consultar despues");

                    if (!ticketsEnEspera.contains(arc)) {
                        ticketsEnEspera.add(arc);
                        contador++;
                    }

                } else {
                    System.out.println("Si existe, actualizando...");
                    con.createStatement().executeUpdate("UPDATE TICKET SET estado ='" + arc.getEstado() + "' WHERE ticket = '" + tic.getTicket() + "'");
                }
            } else {
                //Si no se está anulando se verifica si ya está anulado anteriormente
                ResultSet rs = con.createStatement().executeQuery("SELECT estado FROM TICKET WHERE ticket = '" + tic.getTicket() + "'");
                if (rs.next()) {
                    System.out.println("Fue anulado anteriormente, dejar igual");
                } else {

                    System.out.println("No habia sido anulado, insertando nuevo ticket...");

                    con.createStatement().executeUpdate("DELETE FROM ticket WHERE ticket='" + tic.getTicket() + "'");
                    con.createStatement().executeUpdate("DELETE FROM segmentos WHERE ticket='" + tic.getTicket() + "'");

                    String numero_file = arc.getNumero_file();
                    if (numero_file.length() > 0) {
                        ResultSet resultado;
                        resultado = con.createStatement().executeQuery("SELECT num_file as num_file FROM file_ where num_file = " + numero_file);
                        //Si la consulta no trae una fila, el num_file queda vacío
                        if (!resultado.next()) {
                            numero_file = "";
                        }
                    }

                    if (tic.getTicket() != null && !tic.getTicket().equals("")) {
                        con.createStatement().executeUpdate("INSERT INTO ticket(" + campos_ticket + ") VALUES('"
                                + arc.getNumeroPnr() + "','"
                                + numero_file + "','"
                                + tic.getTicket() + "','"
                                + tic.getOldTicket() + "','"
                                + tic.getCodEmd() + "','"
                                + arc.getFechaEmision_sql() + "','"
                                + arc.getFecha_anulacion_sql() + "','"
                                + arc.getFecha_remision_sql() + "','"
                                + tic.getPosicion() + "','"
                                + tic.getNombrePasajero() + "','"
                                + tic.getTipoPasajero() + "','"
                                + tic.getcLineaAerea() + "','"
                                + arc.getRuta() + "','"
                                + arc.getMoneda() + "','"
                                + arc.getValor_neto() + "','"
                                + arc.getValor_tasas() + "','"
                                + arc.getValor_final() + "','"
                                + tic.getComision() + "','"
                                + tic.getfPago() + "','"
                                + arc.getTipo() + "','"
                                + "" + "','"
                                + 'A'
                                + "'"
                                + ")");

                    }

                    if (!tic.getCodEmd().equals("")) {//tiene tarifas de penalty

                        con.createStatement().executeUpdate("DELETE FROM ticket WHERE ticket='" + tic.getCodEmd() + "'");
                        con.createStatement().executeUpdate("INSERT INTO ticket(" + campos_ticket + ") VALUES('"
                                + arc.getNumeroPnr() + "','"
                                + arc.getNumero_file() + "','"
                                + tic.getCodEmd() + "','"
                                + "" + "','"
                                + "" + "','"
                                + arc.getFecha_remision_sql() + "','"
                                + "" + "','"
                                + "" + "','"
                                + tic.getPosicion() + "','"
                                + tic.getNombrePasajero() + "','"
                                + tic.getTipoPasajero() + "','"
                                + tic.getcLineaAerea() + "','"
                                + arc.getRuta() + "','"
                                + arc.getMoneda() + "','"
                                + "0" + "','"
                                + "0" + "','"
                                + tic.getValorEmd() + "','"
                                + tic.getComision() + "','"
                                + tic.getfPago() + "','"
                                + "EMD','"
                                + "" + "','"
                                + 'A'
                                + "'"
                                + ")");
                    }

                    for (Segmento seg : arc.getSegmentos()) {
                        con.createStatement().executeUpdate("INSERT INTO segmentos(" + campos_segmentos + ") VALUES('"
                                + tic.getTicket() + "','"
                                + seg.getNumeroSegmento() + "','"
                                + seg.getCodSalida() + "','"
                                + seg.getCodLlegada() + "','"
                                + seg.getNomSalida() + "','"
                                + seg.getNomLlegada() + "','"
                                + seg.getFechaSalida() + "','"
                                + seg.getHorSalida() + "','"
                                + seg.getFechaLlegada() + "','"
                                + seg.getHorLLegada() + "','"
                                + seg.getNumeroVuelo() + "','"
                                + seg.getLineaAerea() + "','"
                                + seg.getCodClase() + "'"
                                + ")");
                    }

                }

            }
        }

        con.createStatement().executeUpdate("COMMIT;");

    }

}
