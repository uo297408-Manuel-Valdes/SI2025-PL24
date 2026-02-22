package giis.demo.model;

public class EmpresaDTO {

	private int idEmpresa;
	private String nombre;

	public EmpresaDTO(int idEmpresa, String nombre) {
		this.idEmpresa = idEmpresa;
		this.nombre = nombre;
	}

	public EmpresaDTO() {
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	
	public String toString() {
		return nombre;
	}
}
