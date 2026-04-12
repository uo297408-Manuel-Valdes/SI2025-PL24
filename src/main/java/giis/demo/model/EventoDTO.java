package giis.demo.model;

public class EventoDTO {
	private int idEvento;
	private int idAgencia;
	private String nombre;
	private String fechaEvento;
	private String tematicasTexto;
	private boolean asignacionFinalizada;

	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaEvento) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaEvento = fechaEvento;
	}

	public int getIdEvento() {
		return idEvento;
	}

	public int getIdAgencia() {
		return idAgencia;
	}

	public String getNombre() {
		return nombre;
	}

	public String getFechaEvento() {
		return fechaEvento;
	}

	public String getTematicasTexto() {
		return tematicasTexto;
	}

	public void setTematicasTexto(String tematicasTexto) {
		this.tematicasTexto = tematicasTexto;
	}

	public boolean isAsignacionFinalizada() {
		return asignacionFinalizada;
	}

	public void setAsignacionFinalizada(boolean asignacionFinalizada) {
		this.asignacionFinalizada = asignacionFinalizada;
	}
}