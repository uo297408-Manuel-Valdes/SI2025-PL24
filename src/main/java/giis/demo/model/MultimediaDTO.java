package giis.demo.model;

public class MultimediaDTO {
	 
	private int idMultimedia;
	private int idReportaje;
	private int	idReportero;  
	
	private String path;
	private String tipo;
	
	
	public MultimediaDTO(int idMultimedia, int idReportaje,int idReportero, String path, String tipo) {
		this.idMultimedia = idMultimedia;
		this.idReportaje  = idReportaje;
		this.path         = path;
		this.tipo         = tipo;
		this.idReportero  = idReportero;
	}
 
	public int    getIdMultimedia() { return idMultimedia; }
	public int    getIdReportaje()  { return idReportaje; }
	public String getPath()         { return path; }
	public String getTipo()         { return tipo; }
	public int getIdReportero()         { return idReportero; }
	 
	@Override
	public String toString() { return path; }
}
