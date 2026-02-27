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

	public ReportajeDTO(int idReportaje, int idEvento, String titulo, int idReporteroEntrega) {
		this.idReportaje        = idReportaje;
		this.idEvento           = idEvento;
		this.titulo             = titulo;
		this.idReporteroEntrega = idReporteroEntrega;
	}

	public int    getIdReportaje()        { return idReportaje; }
	public int    getIdEvento()           { return idEvento; }
	public String getTitulo()             { return titulo; }
	public int    getIdReporteroEntrega() { return idReporteroEntrega; }
}
