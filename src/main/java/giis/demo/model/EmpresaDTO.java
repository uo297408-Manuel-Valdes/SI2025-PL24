package giis.demo.model;

public class EmpresaDTO {
	private int idEmpresa;
	private String nombre;
	private String tematicasTexto;
	private int embargos;

	public EmpresaDTO(int idEmpresa, String nombre) {
		this(idEmpresa, nombre, "");
	}
	
	public EmpresaDTO(int idEmpresa, String nombre, int embargos) {
		this(idEmpresa, nombre, "");
		this.embargos=embargos;
	}

	public EmpresaDTO(int idEmpresa, String nombre, String tematicasTexto) {
		this.idEmpresa = idEmpresa;
		this.nombre = nombre;
		this.tematicasTexto = tematicasTexto;
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public String getNombre() {
		return nombre;
	}

	public String getTematicasTexto() {
		return tematicasTexto;
	}

	public void setTematicasTexto(String tematicasTexto) {
		this.tematicasTexto = tematicasTexto;
	}
	
	public int getEmbargos() {
		return embargos;
	}

	@Override
	public String toString() {
		return nombre;
	}
}