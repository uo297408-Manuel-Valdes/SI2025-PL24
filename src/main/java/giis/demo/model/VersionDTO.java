package giis.demo.model;

public class VersionDTO {
	
	private int id_version;
	private int id_reportaje;
	
	private String subtitulo;
	private String cuerpo;
	private String cambios;
	
	public VersionDTO(int id_version, int id_reportaje, String subtitulo, String cuerpo, String cambios) {
		this.id_version=id_version;
		this.id_reportaje=id_reportaje;
		this.subtitulo=subtitulo;
		this.cuerpo=cuerpo;
		this.cambios=cambios;
	}
	public VersionDTO() {}
	public int getId_version() {
		return id_version;
	}
	public void setId_version(int id_version) {
		this.id_version = id_version;
	}
	public int getId_reportaje() {
		return id_reportaje;
	}
	public void setId_reportaje(int id_reportaje) {
		this.id_reportaje = id_reportaje;
	}
	public String getSubtitulo() {
		return subtitulo;
	}
	public void setSubtitulo(String subtitulo) {
		this.subtitulo = subtitulo;
	}
	public String getCuerpo() {
		return cuerpo;
	}
	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}
	public String getCambios() {
		return cambios;
	}
	public void setCambios(String cambios) {
		this.cambios = cambios;
	}
	
	
}
