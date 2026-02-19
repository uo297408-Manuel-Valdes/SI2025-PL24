package giis.demo.model;

public class AgenciaDTO {
    private final int idAgencia;
    private final String nombre;

    public AgenciaDTO(int idAgencia, String nombre) {
        this.idAgencia = idAgencia;
        this.nombre = nombre;
    }
    public int getIdAgencia() { return idAgencia; }
    public String getNombre() { return nombre; }

    @Override public String toString() { return nombre; } 
}
