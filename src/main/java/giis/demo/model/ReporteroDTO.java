package giis.demo.model;

public class ReporteroDTO {
	private int idReportero;
	private int idAgencia;
	private String nombre;
	public ReporteroDTO(int idReportero, int idAgencia, String nombre) {
		super();
		this.idReportero = idReportero;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
	}
	public ReporteroDTO() {
		super();
	}
	public int getIdReportero() {
		return idReportero;
	}
	public void setIdReportero(int idReportero) {
		this.idReportero = idReportero;
	}
	public int getIdAgencia() {
		return idAgencia;
	}
	public void setIdAgencia(int idAgencia) {
		this.idAgencia = idAgencia;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


    
}
