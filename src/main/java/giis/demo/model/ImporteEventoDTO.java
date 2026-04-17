package giis.demo.model;

public class ImporteEventoDTO {
	private int idEvento;
	private String nombreEvento;
	private String fechaInicio;
	private String fechaFin;
	private String provinciaEvento;
	private String paisEvento;
	
	private int idReportero;
	private String nombreReportero;
	private String provinciaReportero;
	
	private int duracionDias;
	private double alojamientoDia;
	private double manutencionDia;
	private double totalDia;
	private double totalEvento;
	public ImporteEventoDTO(int idEvento, String nombreEvento, String fechaInicio, String fechaFin,
			String provinciaEvento, String paisEvento, int idReportero, String nombreReportero,
			String provinciaReportero, int duracionDias, double alojamientoDia, double manutencionDia, double totalDia,
			double totalEvento) {
		super();
		this.idEvento = idEvento;
		this.nombreEvento = nombreEvento;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.provinciaEvento = provinciaEvento;
		this.paisEvento = paisEvento;
		this.idReportero = idReportero;
		this.nombreReportero = nombreReportero;
		this.provinciaReportero = provinciaReportero;
		this.duracionDias = duracionDias;
		this.alojamientoDia = alojamientoDia;
		this.manutencionDia = manutencionDia;
		this.totalDia = totalDia;
		this.totalEvento = totalEvento;
	}
	public int getIdEvento() {
		return idEvento;
	}
	public void setIdEvento(int idEvento) {
		this.idEvento = idEvento;
	}
	public String getNombreEvento() {
		return nombreEvento;
	}
	public void setNombreEvento(String nombreEvento) {
		this.nombreEvento = nombreEvento;
	}
	public String getFechaInicio() {
		return fechaInicio;
	}
	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
	public String getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}
	public String getProvinciaEvento() {
		return provinciaEvento;
	}
	public void setProvinciaEvento(String provinciaEvento) {
		this.provinciaEvento = provinciaEvento;
	}
	public String getPaisEvento() {
		return paisEvento;
	}
	public void setPaisEvento(String paisEvento) {
		this.paisEvento = paisEvento;
	}
	public int getIdReportero() {
		return idReportero;
	}
	public void setIdReportero(int idReportero) {
		this.idReportero = idReportero;
	}
	public String getNombreReportero() {
		return nombreReportero;
	}
	public void setNombreReportero(String nombreReportero) {
		this.nombreReportero = nombreReportero;
	}
	public String getProvinciaReportero() {
		return provinciaReportero;
	}
	public void setProvinciaReportero(String provinciaReportero) {
		this.provinciaReportero = provinciaReportero;
	}
	public int getDuracionDias() {
		return duracionDias;
	}
	public void setDuracionDias(int duracionDias) {
		this.duracionDias = duracionDias;
	}
	public double getAlojamientoDia() {
		return alojamientoDia;
	}
	public void setAlojamientoDia(double alojamientoDia) {
		this.alojamientoDia = alojamientoDia;
	}
	public double getManutencionDia() {
		return manutencionDia;
	}
	public void setManutencionDia(double manutencionDia) {
		this.manutencionDia = manutencionDia;
	}
	public double getTotalDia() {
		return totalDia;
	}
	public void setTotalDia(double totalDia) {
		this.totalDia = totalDia;
	}
	public double getTotalEvento() {
		return totalEvento;
	}
	public void setTotalEvento(double totalEvento) {
		this.totalEvento = totalEvento;
	}
	
	

}
