
package lectorair.lectorarchivo;


public class Ticket {
    private String nombrePasajero;
    private String tipoPasajero;
    private int posicion;
    private String ticket;
    private Double comision;
    private String fPago;
    private String cLineaAerea;//T-K045-5251879214
    private String oldTicket;
    private String codEmd;
    private Double valorEmd;
    
    
    public Double getValorEmd() {
        return valorEmd;
    }

    public void setValorEmd(Double valorEmd) {
        this.valorEmd = valorEmd;
    }
    
    

    public String getCodEmd() {
        return codEmd;
    }

    public void setCodEmd(String codEmd) {
        this.codEmd = codEmd;
    }



    public String getOldTicket() {
        return oldTicket;
    }

    public void setOldTicket(String oldTicket) {
        this.oldTicket = oldTicket;
    }
    
    
    
    public String getfPago() {
        
        if (fPago.length() > 4) {
            if (fPago.substring(0, 2).equals("O/")) {
                return fPago.substring(2, 6);
            }
            return fPago.substring(0, 4);
        }
        return fPago;
    }

    public void setfPago(String fPago) {
        this.fPago = fPago;
    }

    public String getcLineaAerea() {
        return cLineaAerea;
    }

    public void setcLineaAerea(String cLineaAerea) {
        this.cLineaAerea = cLineaAerea;
    }

    public Double getComision() {
        return comision;
    }

    public void setComision(Double comision) {
        this.comision = comision;
    }
    
    
    
    
    Ticket() {
        this.tipoPasajero="ADT";
        this.comision=0.0;
        this.nombrePasajero="";
        this.posicion=0;
        this.ticket="";
        this.fPago="";
        this.cLineaAerea="";//T-K045-5251879214
        this.oldTicket="";
        this.codEmd="";
        this.valorEmd=0.0;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }


    public String getNombrePasajero() {
        return nombrePasajero;
    }

    public void setNombrePasajero(String nombrePasajero) {
        this.nombrePasajero = nombrePasajero;
    }


    public String getTipoPasajero() {
        return tipoPasajero;
    }

    public void setTipoPasajero(String tipoPasajero) {
        if (tipoPasajero.length() <= 3) {
            this.tipoPasajero = tipoPasajero;
        }else{
            this.tipoPasajero = "ADT";
        }
        
    }

    @Override
    public String toString() {
        return "Ticket{" + "nombrePasajero=" + nombrePasajero + ", tipoPasajero=" + tipoPasajero + ", posicion=" + posicion + ", ticket=" + ticket + ", comision=" + comision + ", fPago=" + fPago + ", cLineaAerea=" + cLineaAerea + ", oldTicket=" + oldTicket + ", codEmd=" + codEmd + ", valorEmd=" + valorEmd + '}';
    }

    
    

   
   
    
}
