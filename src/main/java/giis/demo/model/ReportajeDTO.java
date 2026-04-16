package giis.demo.model;

/**
 * Representa una fila de la tabla REPORTAJE.
 * El autor es el reportero que entregó el reportaje (id_reportero_entrega).
 */
public class ReportajeDTO {

	private int    idReportaje;
	private int    idEvento;
	private String titulo;
	private int    idReporteroEntrega;
	private String fecha_embargo;

	public ReportajeDTO(int idReportaje, int idEvento, String titulo, int idReporteroEntrega) {
		this.idReportaje        = idReportaje;
		this.idEvento           = idEvento;
		this.titulo             = titulo;
		this.idReporteroEntrega = idReporteroEntrega;
		this.fecha_embargo		= null;
	}
	
	public ReportajeDTO(int idReportaje, int idEvento, String titulo, int idReporteroEntrega, String fecha_embargo) {
		this.idReportaje        = idReportaje;
		this.idEvento           = idEvento;
		this.titulo             = titulo;
		this.idReporteroEntrega = idReporteroEntrega;
		this.fecha_embargo		= fecha_embargo;
	}

	public int    getIdReportaje()        { return idReportaje; }
	public int    getIdEvento()           { return idEvento; }
	public String getTitulo()             { return titulo; }
	public int    getIdReporteroEntrega() { return idReporteroEntrega; }
	public String getFecha_embargo()      { return fecha_embargo; }
}
