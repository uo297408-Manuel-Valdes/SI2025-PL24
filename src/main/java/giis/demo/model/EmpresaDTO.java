package giis.demo.model;

public class EmpresaDTO {

	private int    idEmpresa;
	private String nombre;
	private String tematicasTexto;
	private int    embargos;
	private String justificante; // usado en ConcederAcceso para mostrar por que es apta

	public EmpresaDTO(int idEmpresa, String nombre) {
		this(idEmpresa, nombre, "");
	}

	public EmpresaDTO(int idEmpresa, String nombre, int embargos) {
		this(idEmpresa, nombre, "");
		this.embargos = embargos;
	}

	public EmpresaDTO(int idEmpresa, String nombre, String tematicasTexto) {
		this.idEmpresa      = idEmpresa;
		this.nombre         = nombre;
		this.tematicasTexto = tematicasTexto;
		this.justificante   = "";
	}

	// Constructor con justificante — usado unicamente en ConcederAccesoModel
	public EmpresaDTO(int idEmpresa, String nombre, int embargos, String justificante) {
		this.idEmpresa      = idEmpresa;
		this.nombre         = nombre;
		this.tematicasTexto = "";
		this.embargos       = embargos;
		this.justificante   = justificante;
	}

	public int    getIdEmpresa()    { return idEmpresa; }
	public String getNombre()       { return nombre; }
	public String getTematicasTexto() { return tematicasTexto; }
	public int    getEmbargos()     { return embargos; }
	public String getJustificante() { return justificante; }

	public void setTematicasTexto(String tematicasTexto) { this.tematicasTexto = tematicasTexto; }
	public void setJustificante(String j)                { this.justificante = j; }

	@Override
	public String toString() { return nombre; }
}