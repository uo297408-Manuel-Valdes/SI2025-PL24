package giis.demo.model;

public class VersionReportajeDTO {

	private int    idVersion;
	private int    idReportaje;
	private String subtitulo;
	private String cuerpo;
	private String cambios;

	public VersionReportajeDTO(int idVersion, int idReportaje,
	                            String subtitulo, String cuerpo, String cambios) {
		this.idVersion  = idVersion;
		this.idReportaje = idReportaje;
		this.subtitulo  = subtitulo;
		this.cuerpo     = cuerpo;
		this.cambios    = cambios;
	}

	public int    getIdVersion()   { return idVersion; }
	public int    getIdReportaje() { return idReportaje; }
	public String getSubtitulo()   { return subtitulo; }
	public String getCuerpo()      { return cuerpo; }
	public String getCambios()     { return cambios; }
}
