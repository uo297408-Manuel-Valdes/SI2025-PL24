package giis.demo.model;

public class AccesoDTO {
	
	private int idAcceso;
	private int idEvento;
	private int idAgencia;
	private int descargado;
	
	public AccesoDTO(int idAcceso, int idEvento, int idAgencia, int descargado) {
		this.idAcceso=idAcceso;
		this.idEvento=idEvento;
		this.idAgencia=idAgencia;
		this.descargado=descargado;
	}
	

	public AccesoDTO() {
		super();
	}

	public int getIdAcceso() {
		return idAcceso;
	}

	public int getIdEvento() {
		return idEvento;
	}

	public int getIdAgencia() {
		return idAgencia;
	}

	public int getDescargado() {
		return descargado;
	}
	
}
