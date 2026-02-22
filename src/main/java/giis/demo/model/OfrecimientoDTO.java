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
	public OfrecimientoDTO(
			int idOfrecimiento,
			int idEvento,
			int idAgencia,
			int idEmpresa,
			String nombreEvento,
			String fechaEvento,
			String nombreAgencia,
			String decision) {

		this.idOfrecimiento = idOfrecimiento;
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.idEmpresa = idEmpresa;
		this.nombreEvento = nombreEvento;
		this.fechaEvento = fechaEvento;
		this.nombreAgencia = nombreAgencia;
		this.decision = decision;
	}

	public OfrecimientoDTO() {
	}

	public int getIdOfrecimiento() {
		return idOfrecimiento;
	}

	public void setIdOfrecimiento(int idOfrecimiento) {
		this.idOfrecimiento = idOfrecimiento;
	}

	public int getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(int idEvento) {
		this.idEvento = idEvento;
	}

	public int getIdAgencia() {
		return idAgencia;
	}

	public void setIdAgencia(int idAgencia) {
		this.idAgencia = idAgencia;
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public String getNombreEvento() {
		return nombreEvento;
	}

	public void setNombreEvento(String nombreEvento) {
		this.nombreEvento = nombreEvento;
	}

	public String getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(String fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public String getNombreAgencia() {
		return nombreAgencia;
	}

	public void setNombreAgencia(String nombreAgencia) {
		this.nombreAgencia = nombreAgencia;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}
}
