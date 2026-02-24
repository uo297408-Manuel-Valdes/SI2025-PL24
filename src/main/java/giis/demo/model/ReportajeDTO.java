package giis.demo.model;

public class ReportajeDTO {
    private int idReportaje;
    private int idEvento;
    private int idReportero;
    private String autor;
    private String titulo;
    private String subtitulo;
    private String cuerpo;

    public ReportajeDTO(int idReportaje, int idEvento, int idReportero,
                        String autor, String titulo, String subtitulo, String cuerpo) {
        this.idReportaje = idReportaje;
        this.idEvento = idEvento;
        this.idReportero = idReportero;
        this.autor = autor;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.cuerpo = cuerpo;
    }

    public int getIdReportaje() { return idReportaje; }
    public int getIdEvento() { return idEvento; }
    public int getIdReportero() { return idReportero; }
    public String getAutor() { return autor; }
    public String getTitulo() { return titulo; }
    public String getSubtitulo() { return subtitulo; }
    public String getCuerpo() { return cuerpo; }
}