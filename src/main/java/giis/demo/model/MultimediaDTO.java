package giis.demo.model;

public class MultimediaDTO {
	private int id_multimedia;
	private int id_reportaje;
	private int id_reportero;
	private String tipo;
	private String ruta;
	private String estado;
	
	public MultimediaDTO(int id_multimedia,int id_reportero, int id_reportaje, String tipo, String estado, String ruta) {
		this.id_multimedia=id_multimedia;
		this.id_reportero=id_reportero;
		this.id_reportaje=id_reportaje;
		this.tipo=tipo;
		this.ruta=ruta;
		this.estado=estado;
	}
	
	public int getId_multimedia() {
		return id_multimedia;
	}

	public int getId_reportaje() {
		return id_reportaje;
	}

	public int getId_reportero() {
		return id_reportero;
	}

	public String getTipo() {
		return tipo;
	}

	public String getRuta() {
		return ruta;
	}

	public String getEstado() {
		return estado;
	}
	
}
