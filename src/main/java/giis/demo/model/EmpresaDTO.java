package giis.demo.model;

public class EmpresaDTO {
	private final int idEmpresa;
    private final String nombre;

    public EmpresaDTO(int idAgencia, String nombre) {
        this.idEmpresa = idAgencia;
        this.nombre = nombre;
    }
    public int getIdEmpresa() { return idEmpresa; }
    public String getNombre() { return nombre; }

    @Override public String toString() { return nombre; } 
}
