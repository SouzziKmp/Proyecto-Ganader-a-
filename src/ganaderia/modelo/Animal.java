package ganaderia.modelo;

import java.util.Date;

public class Animal {
    private int    idAnimal;
    private int    idPotrero;
    private String codigoArete;
    private String raza;
    private String sexo;
    private Date   fechaNacimiento;
    private double pesoKg;
    private String estado;
    private String potrero;   // nombre del potrero (para reportes)

    public int getIdAnimal() { return idAnimal; }
    public void setIdAnimal(int v) { this.idAnimal = v; }

    public int getIdPotrero() { return idPotrero; }
    public void setIdPotrero(int v) { this.idPotrero = v; }

    public String getCodigoArete() { return codigoArete; }
    public void setCodigoArete(String v) { this.codigoArete = v; }

    public String getRaza() { return raza; }
    public void setRaza(String v) { this.raza = v; }

    public String getSexo() { return sexo; }
    public void setSexo(String v) { this.sexo = v; }

    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date v) { this.fechaNacimiento = v; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double v) { this.pesoKg = v; }

    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }

    public String getPotrero() { return potrero; }
    public void setPotrero(String v) { this.potrero = v; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %.1f kg | %s | Potrero: %s",
                codigoArete, raza, sexo.equals("H") ? "Hembra" : "Macho",
                estado, pesoKg, fechaNacimiento, potrero);
    }
}
