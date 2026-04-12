package giis.demo.model;

public class EventoDTO {
	private int idEvento;
	private int idAgencia;
	private String nombre;
	private String fechaEvento; 
	private String tematicasTexto;
	private int finalizada;

	
	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaEvento) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaEvento = fechaEvento;
		this.tematicasTexto = "";
		this.finalizada= -1;
	}
	
	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaEvento, int finalizada) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaEvento = fechaEvento;
		this.tematicasTexto = "";
		this.finalizada=finalizada;
	}

	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaEvento, String tematicasTexto) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaEvento = fechaEvento;
		this.tematicasTexto = tematicasTexto;
		this.finalizada=-1;
	}
	

	public EventoDTO() {
		super();
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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(String fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public String getTematicasTexto() {
		return tematicasTexto;
	}
	
	public int getFinalizada() {
		return finalizada;
	}

	public void setTematicasTexto(String tematicasTexto) {
		this.tematicasTexto = tematicasTexto;
	}
}