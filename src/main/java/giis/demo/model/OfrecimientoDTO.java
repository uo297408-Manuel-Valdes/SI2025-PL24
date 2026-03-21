package giis.demo.model;

public class OfrecimientoDTO {
	private int idOfrecimiento;
	private int idEvento;
	private int idAgencia;
	private int idEmpresa;
	private String nombreEvento;
	private String fechaEvento;
	private String nombreAgencia;
	private String decision;
	private String tematicasTexto;
	
	

	public OfrecimientoDTO() {
		super();
	}

	public OfrecimientoDTO(int idOfrecimiento, int idEvento, int idEmpresa, String decision) {
		this.idOfrecimiento=idOfrecimiento;
		this.idEvento=idEvento;
		this.idEmpresa=idEmpresa;
		this.decision=decision;
		this.nombreEvento=null;
		this.nombreAgencia=null;
		this.fechaEvento=null;
		this.tematicasTexto=null;
		this.idAgencia=0;
	}
	
	public OfrecimientoDTO(int idOfrecimiento, int idEvento, int idAgencia, int idEmpresa,
			String nombreEvento, String fechaEvento, String nombreAgencia,
			String decision, String tematicasTexto) {
		this.idOfrecimiento = idOfrecimiento;
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.idEmpresa = idEmpresa;
		this.nombreEvento = nombreEvento;
		this.fechaEvento = fechaEvento;
		this.nombreAgencia = nombreAgencia;
		this.decision = decision;
		this.tematicasTexto = tematicasTexto;
	}

	public int getIdOfrecimiento() {
		return idOfrecimiento;
	}

	public int getIdEvento() {
		return idEvento;
	}

	public int getIdAgencia() {
		return idAgencia;
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public String getNombreEvento() {
		return nombreEvento;
	}

	public String getFechaEvento() {
		return fechaEvento;
	}

	public String getNombreAgencia() {
		return nombreAgencia;
	}

	public String getDecision() {
		return decision;
	}

	public String getTematicasTexto() {
		return tematicasTexto;
	}
}