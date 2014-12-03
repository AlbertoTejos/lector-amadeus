package lectorair.lectorarchivo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import lectorair.datos.db.ArchivoDAO;
import lectorair.datos.db.Parametros;

public class Archivo {

    private File archivo;
    private String numeroPnr;
    private String fechaEmision;
    private String moneda;
    private double valor_neto;
    private double valor_final;
    private double valor_tasas; //tasas embarque tax
    private String numero_file;//numero de negocio se extrae desde parametros.conf
    private String fecha_anulacion;
    private String fecha_remision;
    private String ruta;
    private String tipo;
    private String estado;
    private ArrayList<Ticket> pajaseros;
    private ArrayList<Segmento> segmentos;
    private ArrayList<String> bf;

    public Archivo(File archivo) throws FileNotFoundException, IOException {
        this.archivo = archivo;
        this.numeroPnr = "";
        this.fechaEmision = "";
        this.moneda = "";
        this.valor_neto = 0.0;
        this.valor_final = 0.0;
        this.valor_tasas = 0.0;
        this.numero_file = "";
        this.fecha_anulacion = "";
        this.fecha_remision = "";
        this.ruta = "";
        this.tipo = "TKT";
        this.estado = "";
        this.pajaseros = new ArrayList<>();
        this.segmentos = new ArrayList<>();
        this.bf = new ArrayList<>();
        initReader();
        this.iniciarArchivo();
    }

    private void initReader() throws FileNotFoundException, IOException {
        BufferedReader bfi = new BufferedReader(new FileReader(archivo));
        String cadena;
        while ((cadena = bfi.readLine()) != null) {
            if (!cadena.equals("")) {
                bf.add(cadena);
            }
        }

        System.out.println("Contenido del archivo: " + bf);
        bfi.close();
    }

    private void iniciarArchivo() throws FileNotFoundException, IOException {
        String isNulo = getDatoPunto("AMD ", 2, 0);
        if (isNulo.length() > 4) {
            isNulo = isNulo.substring(0, 4);
        }
        String isNuloR = getDatoPunto("AMD", 2, 0);
        if (isNuloR.length() > 4) {
            isNuloR = isNuloR.substring(0, 4);
        }
        if (isNulo.equalsIgnoreCase("VOID") || isNuloR.equalsIgnoreCase("VOID")) {
            iniciarArchivoAnulacion();
        } else {
            if (existeLinea("RFDF")) {
                iniciarArchivoDevolucion();
            } else {
                iniciarArchivoTicket();
            }
        }

    }

    public void iniciarArchivoAnulacion() throws FileNotFoundException, IOException {
        this.setEstado("ANULADO");
        int cantidadPersonas = getIncidencias("I-");
        for (int i = 0; i < cantidadPersonas; i++) {
            Ticket tic = new Ticket();
            tic.setTicket(getDatoGuion("T-", 1, i));
            this.pajaseros.add(tic);
        }
    }

    public void iniciarArchivoDevolucion() throws FileNotFoundException, IOException {

        this.setTipo("RFD");
        this.setFechaEmision(getDatoPunto("D-", 0, 0));
        this.setMoneda(getDatoPunto("RFDF", 3, 0).substring(0, 3));
        this.setValor_final(Double.parseDouble(getDatoPunto("RFDF", 12, 0).trim()));
        this.setValor_neto(Double.parseDouble(getDatoPunto("RFDF", 3, 0).trim().substring(3)));
        this.setValor_tasas(Double.parseDouble(getDatoPunto("RFDF", 11, 0).substring(2).trim()));
        //this.setValor_neto(getPrecioNeto());
        //this.cargaTasas();      
        int cantidadPersonas = getIncidencias("I-");
        for (int i = 0; i < cantidadPersonas; i++) {
            Ticket tic = new Ticket();
            tic.setNombrePasajero(getDatoPunto("I-", 1, i).substring(2));
            tic.setPosicion(Integer.parseInt(getDatoPunto("I-", 1, i).substring(0, 2)));
            tic.setTicket(getDatoGuion("T-", 1, i));
            tic.setfPago(getDatoPunto("FP", 0, i));
            tic.setComision(Double.parseDouble(dividirPorAsterisco(getDatoPunto("FM", 0, i), 2)));
            this.pajaseros.add(tic);
        }
    }

    public void iniciarArchivoTicket() throws FileNotFoundException, IOException {

        try {
            this.setNumeroPnr(dividirPorEspacio(getDatoPunto("MUC", 0, 0), 1).substring(0, 6));
        } catch (Exception e) {
            this.setNumeroPnr("");
        }

        this.setFechaEmision(getDatoPunto("D-", 2, 0));
        this.setNumero_file(getNumFile());

        /**
         * ***************TARIFA*********************** - La tarifa viene en la
         * linea K- , en caso de no venir se reccure a la linea KN- - Los Tax de
         * extraen con el metodo getPrecioNeto() en caso de no venir nada en ela
         * linea K- se sacan de la KNT
         */
        if (!getDatoPunto("K-", 0, 0).equals("")) {
            String moneda = getDatoPunto("K-", 12, 0).substring(0, 3);
            double tipoCambio = buscarTipoCambioK();
            if (moneda.equals("CLP") && tipoCambio > 0) {
                    setMoneda(moneda);
                    setValor_final(Double.parseDouble(getDatoPunto("K-", 12, 0).substring(3).trim()));
                    setValor_neto(getPrecioNeto()*tipoCambio);
                    cargaTasas();
                } else {
                    setMoneda(getDatoPunto("K-", 0, 0).substring(1, 4));
                    setValor_neto(getPrecioNeto());
                    cargaTasas();
                    setValor_final(getValor_neto()+getValor_tasas());
                }

        } else {
            if (existeLinea("KN-")) {
                String moneda = getDatoPunto("KN-", 12, 0).substring(0, 3);
                double tipoCambio = buscarTipoCambioKN();
                if (moneda.equals("CLP") && tipoCambio > 0) {
                    setMoneda(moneda);
                    setValor_final(Double.parseDouble(getDatoPunto("KN-", 12, 0).substring(3).trim()));
                    setValor_neto(getPrecioNetoKN()*tipoCambio);
                    cargaTasasKNT();
                } else {
                    setMoneda(getDatoPunto("KN-", 0, 0).substring(1, 4));
                    setValor_neto(getPrecioNetoKN());
                    cargaTasasKNT();
                    setValor_final(getValor_neto()+getValor_tasas());
                }

            }
        }

        /**
         * ********************************************
         */
        /**
         * ********************************************
         */
        //this.setNumero_file(getNumFile()); 
        //Personas
        int cantidadPersonas = getIncidencias("I-");
        for (int i = 0; i < cantidadPersonas; i++) {
            Ticket per = new Ticket();
            if (existeLinea("T-")) {

                per.setNombrePasajero(getDatoPunto("I-", 1, i).substring(2));
                per.setPosicion(Integer.parseInt(getDatoPunto("I-", 1, i).substring(0, 2)));
                per.setTicket(getDatoGuion("T-", 1, i));
                per.setcLineaAerea(getDatoGuion("T-", 0, i).substring(1));
                per.setfPago(getDatoPunto("FP", 0, i));
                buscaTipoPersona(per);
                per.setComision(Double.parseDouble(dividirPorAsterisco(getDatoPunto("FM", 0, i), 2)));
                this.pajaseros.add(per);
                //Es una reemision
            }

            if (existeLinea("TMC")) {
                //si es una reemision
                per.setCodEmd(dividirPorGuion(getDatoPunto("TMC", 0, i), 1));
                this.fecha_remision = this.fechaEmision;
                int cantidadFoi = getIncidencias("FOI");
                String oldticket = dividirPorSlash(dividirPorGuion(getDatoPunto("FO", 0, i + cantidadFoi), 1), 0);
                if (oldticket.length() > 10) {
                    per.setOldTicket(oldticket.substring(0, oldticket.length() - 10));
                } else {
                    per.setOldTicket(oldticket);
                }
                if (existeLinea("ATC")) {
                    per.setValorEmd(Double.parseDouble(getDatoPunto("ATC-", 8, 0).substring(3)));
                } else {
                    per.setValorEmd(Double.parseDouble(getDatoPunto("EMD", 28, 0).substring(3)));
                }

                this.pajaseros.add(per);

            }
        }

        //56392819
        //segmentos
        int cantidadSegmentos = getIncidencias("H-");
        for (int i = 0; i < cantidadSegmentos; i++) {
            if (!getDatoPunto("H-", 5, i).trim().equals("VOID")) {
                Segmento seg = new Segmento();
                seg.setNumeroSegmento(Integer.parseInt(getDatoPunto("H-", 0, i)));
                seg.setCodSalida(getDatoPunto("H-", 1, i).substring(getDatoPunto("H-", 1, i).length() - 3));
                seg.setCodLlegada(getDatoPunto("H-", 3, i));
                seg.setNomSalida(getDatoPunto("H-", 2, i));
                seg.setNomLlegada(getDatoPunto("H-", 4, i));
                seg.setFechaSalida(dividirPorEspacio(getDatoPunto("H-", 5, i), 4).substring(0, 5));
                seg.setFechaLlegada(dividirPorEspacio(getDatoPunto("H-", 5, i), 6));
                seg.setHorSalida(dividirPorEspacio(getDatoPunto("H-", 5, i), 4).substring(5));
                seg.setHorLLegada(dividirPorEspacio(getDatoPunto("H-", 5, i), 5));
                seg.setNumeroVuelo(dividirPorEspacio(getDatoPunto("H-", 5, i), 1));
                seg.setLineaAerea(dividirPorEspacio(getDatoPunto("H-", 5, i), 0));
                seg.setCodClase(dividirPorEspacio(getDatoPunto("H-", 5, i), 3));
                this.segmentos.add(seg);
            }
        }

    }

    public double getPrecioNeto() throws FileNotFoundException, IOException {
        double precio1;
        String codigo1 = getDatoPunto("K-", 0, 0).substring(1, 4);
        String codigoFinal = getDatoPunto("K-", 12, 0).substring(0, 3);
        if (codigo1.equals(codigoFinal)) {
            precio1 = Double.parseDouble(getDatoPunto("K-", 0, 0).substring(4).trim());
        } else {
            precio1 = Double.parseDouble(getDatoPunto("K-", 0, 0).substring(4).trim()); //substring 3, posicion 0
        }
        return precio1;
    }

    public double getPrecioNetoKN() throws FileNotFoundException, IOException {
        double precio1;
        String codigo1 = getDatoPunto("KN-", 0, 0).substring(1, 4);
        String codigoFinal = getDatoPunto("KN-", 12, 0).substring(0, 3);
        if (codigo1.equals(codigoFinal)) {
            precio1 = Double.parseDouble(getDatoPunto("KN-", 0, 0).substring(4).trim());
        } else {
            precio1 = Double.parseDouble(getDatoPunto("KN-", 0, 0).substring(4).trim()); //substring 3, posicion 0

        }
        return precio1;
    }

    //delimitador ;
    private String getDatoPunto(String linea, int posicion, int incidencia) throws FileNotFoundException, IOException {
        int cont = 0;

        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() >= linea.length()) {
                if (cadena.substring(0, linea.length()).equals(linea)) {
                    if (cont == incidencia) {
                        String cadena_final = cadena.substring(linea.length());
                        String[] split = cadena_final.split(";");
                        return split[posicion].trim();
                    }
                    cont++;
                }
            }
        }

        return "";
    }

    private boolean existeLinea(String linea) throws FileNotFoundException, IOException {
        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() >= linea.length()) {
                if (cadena.substring(0, linea.length()).equals(linea)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String dividirPorAsterisco(String linea, int posicion) throws FileNotFoundException, IOException {
        linea = quitaEspacios(linea);
        String[] split = linea.split("\\*");
        if (split.length > posicion) {
            return split[posicion].trim();
        } else {
            return "0";
        }
    }

    //delimitador - 
    private String getDatoGuion(String linea, int posicion, int incidencia) throws FileNotFoundException, IOException {
        int cont = 0;
        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() > linea.length()) {
                if (cadena.substring(0, linea.length()).equals(linea)) {
                    if (cont == incidencia) {
                        String cadena_final = cadena.substring(linea.length());
                        String[] split = cadena_final.split("-");
                        return split[posicion].trim();
                    }
                    cont++;
                }
            }
        }
        return "";
    }

    private String dividirPorEspacio(String linea, int posicion) throws FileNotFoundException, IOException {
        linea = quitaEspacios(linea);
        String[] split = linea.split(" ");
        return split[posicion].trim();
    }

    private String dividirPorSlash(String linea, int posicion) throws FileNotFoundException, IOException {
        linea = quitaEspacios(linea);
        String[] split = linea.split("/");
        return split[posicion].trim();
    }

    private String dividirPorGuion(String linea, int posicion) throws FileNotFoundException, IOException {
        linea = quitaEspacios(linea);
        String[] split = linea.split("-");
        if (split.length - 1 >= posicion) {
            return split[posicion].trim();
        }
        return "";
    }

    public String quitaEspacios(String texto) {
        texto = texto.replaceAll(" +", " ");
        return texto.trim();
    }

    private int getIncidencias(String linea) throws FileNotFoundException, IOException {
        int cont = 0;
        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() > linea.length()) {
                if (cadena.substring(0, linea.length()).equals(linea)) {
                    cont++;
                }
            }
        }
        return cont;
    }

    private void buscaTipoPersona(Ticket tic) {
        int inicio = tic.getNombrePasajero().indexOf("(");
        if (inicio > 0) {
            int fin = tic.getNombrePasajero().indexOf(")");
            tic.setTipoPasajero(tic.getNombrePasajero().substring(inicio + 1, fin));
            tic.setNombrePasajero(tic.getNombrePasajero().substring(0, inicio));

        } else {
            tic.setTipoPasajero("ADT");
        }
    }

    private void cargaTasas() throws FileNotFoundException, IOException {
        //BufferedReader bf = new BufferedReader(new FileReader(archivo));
        double valor = 0;
        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() > 3) {
                if (cadena.substring(0, 3).equals("KFT")) {
                    String cadena_final = cadena.substring(4);
                    String[] split = cadena_final.split(";");
                    for (String string : split) {
                        String[] valores_string = string.trim().split(" ");
                        if (valores_string[0].length() > 0) {
                            double valor_aux = Double.parseDouble(valores_string[0].substring(getPosicionNumero(valores_string[0])));
                            valor += valor_aux;
                        }
                    }
                }
            }
        }
        this.setValor_tasas(valor);
    }

    private void cargaTasasKNT() throws FileNotFoundException, IOException {
        //BufferedReader bf = new BufferedReader(new FileReader(archivo));
        double valor = 0;
        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() > 3) {
                if (cadena.substring(0, 3).equals("KNT")) {
                    String cadena_final = cadena.substring(4);
                    String[] split = cadena_final.split(";");
                    for (String string : split) {
                        String[] valores_string = string.trim().split(" ");
                        if (valores_string[0].length() > 0) {
                            double valor_aux = Double.parseDouble(valores_string[0].substring(getPosicionNumero(valores_string[0])));
                            valor += valor_aux;
                        }
                    }
                }
            }
        }
        this.setValor_tasas(valor);
    }

    private int getPosicionNumero(String texto) {

        char[] array = texto.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (Character.isDigit(array[i])) {
                return i;
            }
        }

        return 0;
    }

    private String getNumFile() throws FileNotFoundException, IOException {
        String incidencia = Parametros.getInstance().getSegmento_numfile();
        //INCIDENCIA RM*FFA=
        System.out.println(incidencia);
        int largo = incidencia.length();
        //LARGO 7
        for (String cadena : bf) {
            if (!cadena.equals("") && cadena.length() > largo && !incidencia.equals("")) {
                //System.out.println(incidencia+" - "+cadena.substring(0,largo));
                if (cadena.substring(0, largo).equals(incidencia)) {
                    String cadena_final = cadena.substring(largo).trim();
                    String replaceAll = cadena_final.replaceAll("\\D+", "");
                    String[] split;
                    split = replaceAll.split(" ");
                    System.out.println("num_file : " + split[0]);
                    return split[0];
                }
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "Archivo{" + "archivo=" + archivo + ", numeroPnr=" + numeroPnr + ", fechaEmision=" + fechaEmision + ", moneda=" + moneda + ", valor_neto=" + valor_neto + ", valor_final=" + valor_final + ", valor_tasas=" + valor_tasas + ", numero_file=" + numero_file + ", fecha_anulacion=" + fecha_anulacion + ", fecha_remision=" + fecha_remision + ", ruta=" + ruta + ", tipo=" + tipo + ", estado=" + estado + ", pajaseros=" + pajaseros + ", segmentos=" + segmentos + '}';
    }

    public File getArchivo() {
        return archivo;
    }

    public void setArchivo(File archivo) {
        this.archivo = archivo;
    }

    public ArrayList<Ticket> getPajaseros() {
        return pajaseros;
    }

    public void setPajaseros(ArrayList<Ticket> pajaseros) {
        this.pajaseros = pajaseros;
    }

    public ArrayList<Segmento> getSegmentos() {
        return segmentos;
    }

    public void setSegmentos(ArrayList<Segmento> segmentos) {
        this.segmentos = segmentos;
    }

    public String getNumeroPnr() {
        return numeroPnr;
    }

    public String getNumero_file() {
        return numero_file;
    }

    public void setNumero_file(String numero_file) {
        this.numero_file = numero_file;
    }

    public String getRuta() {
        ruta = "";
        if (this.segmentos.size() > 0) {

            for (Segmento seg : this.segmentos) {
                ruta += seg.getCodSalida() + "/";
            }
            ruta += this.segmentos.get(segmentos.size() - 1).getCodLlegada();
        }
        return ruta;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setNumeroPnr(String numeroPnr) {
        this.numeroPnr = numeroPnr;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getFechaEmision_sql() {
        if (fechaEmision.equals("")) {
            return "";
        }
        return getFechaSQL(fechaEmision);
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public double getValor_neto() {
        return valor_neto;
    }

    public void setValor_neto(double valor_neto) {
        this.valor_neto = valor_neto;
    }

    public double getValor_final() {
        return valor_final;
    }

    public void setValor_final(double valor_final) {
        this.valor_final = valor_final;
    }

    public double getValor_tasas() {
        return valor_tasas;
    }

    public void setValor_tasas(double valor_tasas) {
        this.valor_tasas = valor_tasas;
    }

    public String getFecha_anulacion() {
        return fecha_anulacion;
    }

    public String getFecha_anulacion_sql() {
        if (fecha_anulacion.equals("")) {
            return "";
        }
        return getFechaSQL(fecha_anulacion);
    }

    public void setFecha_anulacion(String fecha_anulacion) {
        this.fecha_anulacion = fecha_anulacion;
    }

    public String getFecha_reemision() {
        return fecha_remision;
    }

    public String getFecha_remision_sql() {
        if (fecha_remision.equals("")) {
            return "";
        }
        return getFechaSQL(fecha_remision);
    }

    public void setFecha_remision(String fecha_reemision) {
        this.fecha_remision = fecha_reemision;
    }

    private String getFechaSQL(String fecha) {
        if (fecha.length() == 6) {

            String fechaFinal = fecha.substring(4) + "-" + fecha.substring(2, 4) + "-" + "20" + fecha.substring(0, 2);
            System.out.println(fechaFinal);
            return fechaFinal;

        }
        return "";
    }

    public String getTipo() {
        if (tipo.trim().equals("")) {
            return "TKT";
        }
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public static void main(String[] args) {
        try {
            Archivo lc = new Archivo(new File("C:\\Users\\Alberto\\Desktop\\Pruebas\\lectura\\@0100009.APR"));
            System.out.println(lc);
            ArchivoDAO a = new ArchivoDAO();
            try {
                a.insertArchivo(lc);
            } catch (SQLException ex) {
                Logger.getLogger(Archivo.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Archivo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Archivo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private double buscarTipoCambioKN() throws IOException {

        if (getDatoPunto("KN-", 13, 0).equals("")) {
            return 0;
        }

        return Double.parseDouble(getDatoPunto("KN-", 13, 0));

    }
    
    private double buscarTipoCambioK() throws IOException {

        if (getDatoPunto("K-", 13, 0).equals("")) {
            return 0;
        }

        return Double.parseDouble(getDatoPunto("K-", 13, 0));

    }

}
