package giis.demo.model;

public class EventoDTO {
	private int idEvento;
	private int idAgencia;
	private String nombre;
	private String fechaInicio;
	private String fechaFin;
	private String tematicasTexto;
	private boolean asignacionFinalizada;

	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaInicio, String fechaFin) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.tematicasTexto = "";
		this.asignacionFinalizada = false;
	}

	// Compatibilidad con HU antiguas que solo tenían una fecha
	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaEvento) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaInicio = fechaEvento;
		this.fechaFin = fechaEvento;
		this.tematicasTexto = "";
		this.asignacionFinalizada = false;
	}

	// Compatibilidad con código antiguo que pasa finalizada como int
	public EventoDTO(int idEvento, int idAgencia, String nombre, String fechaEvento, int finalizada) {
		this.idEvento = idEvento;
		this.idAgencia = idAgencia;
		this.nombre = nombre;
		this.fechaInicio = fechaEvento;
		this.fechaFin = fechaEvento;
		this.tematicasTexto = "";
		this.asignacionFinalizada = (finalizada == 1);
	}

	public EventoDTO() {
		this.tematicasTexto = "";
		this.asignacionFinalizada = false;
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

	// Compatibilidad con código antiguo
	public String getFechaEvento() {
		return fechaInicio;
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

	// Compatibilidad con código antiguo
	public int getFinalizada() {
		return asignacionFinalizada ? 1 : 0;
	}

	public String getRangoFechasTexto() {
		if (fechaInicio == null || fechaFin == null) {
			return "";
		}
		if (fechaInicio.equals(fechaFin)) {
			return fechaInicio;
		}
		return fechaInicio + " a " + fechaFin;
	}

	@Override
	public String toString() {
		return nombre;
	}
}