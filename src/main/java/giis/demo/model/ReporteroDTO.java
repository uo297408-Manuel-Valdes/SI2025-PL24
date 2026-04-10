package giis.demo.model;

public class ReporteroDTO {
	private int idReportero;
	private int idAgencia;
	private String nombre;
	private String tematicasTexto;
	private String tipoReportero;
	private boolean responsable;

	public ReporteroDTO(int idReportero, int idAgencia, String nombre, String tematicasTexto, String tipoReportero) {
		this.idReportero = idReportero;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.tematicasTexto = tematicasTexto;
		this.tipoReportero = tipoReportero;
		this.responsable = false;
	}

	public int getIdReportero() {
		return idReportero;
	}

	public int getIdAgencia() {
		return idAgencia;
	}

	public String getNombre() {
		return nombre;
	}

	public String getTematicasTexto() {
		return tematicasTexto;
	}

	public String getTipoReportero() {
		return tipoReportero;
	}

	public boolean isResponsable() {
		return responsable;
	}

	public void setResponsable(boolean responsable) {
		this.responsable = responsable;
	}

	@Override
	public String toString() {
		return nombre;
	}
}