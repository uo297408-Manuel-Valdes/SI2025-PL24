package giis.demo.model;

public class ReportajeDTO {

	private int id_reportaje;
	private int id_evento;
	private int id_reportero_entrega;
	
	private String titulo;
	
	public ReportajeDTO(int id_reportaje, int id_evento, int id_reportero_entrega, String titulo) {
		this.id_reportaje=id_reportaje;
		this.id_evento=id_evento;
		this.id_reportero_entrega=id_reportero_entrega;
		this.titulo=titulo;
	}
	
	public ReportajeDTO() {}

	public int getId_reportaje() {
		return id_reportaje;
	}

	public void setId_reportaje(int id_reportaje) {
		this.id_reportaje = id_reportaje;
	}

	public int getId_evento() {
		return id_evento;
	}

	public void setId_evento(int id_evento) {
		this.id_evento = id_evento;
	}

	public int getId_reportero_entrega() {
		return id_reportero_entrega;
	}

	public void setId_reportero_entrega(int id_reportero_entrega) {
		this.id_reportero_entrega = id_reportero_entrega;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	
	
	
}
