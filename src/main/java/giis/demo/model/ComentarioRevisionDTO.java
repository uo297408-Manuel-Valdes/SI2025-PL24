package giis.demo.model;

public class ComentarioRevisionDTO {
    private int    idComentario;
    private int    idReportaje;
    private int    idReportero;
    private String comentario;
    private String fechaHora;

    public ComentarioRevisionDTO(int idComentario, int idReportaje,
                                  int idReportero, String comentario, String fechaHora) {
        this.idComentario = idComentario;
        this.idReportaje  = idReportaje;
        this.idReportero  = idReportero;
        this.comentario   = comentario;
        this.fechaHora    = fechaHora;
    }

    public int    getIdComentario() { return idComentario; }
    public int    getIdReportaje()  { return idReportaje; }
    public int    getIdReportero()  { return idReportero; }
    public String getComentario()   { return comentario; }
    public String getFechaHora()    { return fechaHora; }
}