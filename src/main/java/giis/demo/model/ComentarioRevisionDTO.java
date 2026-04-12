package giis.demo.model;
 
public class ComentarioRevisionDTO {
    private int    idComentario;
    private int    idReportaje;
    private int    idReportero;
    private String comentario;
    private String fechaHora;
    private String estado;
    private String autor;
 
    public ComentarioRevisionDTO(int idComentario, int idReportaje,
                                  int idReportero, String comentario,
                                  String fechaHora, String estado) {
        this(idComentario, idReportaje, idReportero, comentario, fechaHora, estado, "");
    }
 

    public ComentarioRevisionDTO(int idComentario, int idReportaje,
                                  int idReportero, String comentario,
                                  String fechaHora, String estado, String autor) {
        this.idComentario = idComentario;
        this.idReportaje  = idReportaje;
        this.idReportero  = idReportero;
        this.comentario   = comentario;
        this.fechaHora    = fechaHora;
        this.estado       = estado;
        this.autor        = autor;
    }
 
    public int    getIdComentario() { return idComentario; }
    public int    getIdReportaje()  { return idReportaje; }
    public int    getIdReportero()  { return idReportero; }
    public String getComentario()   { return comentario; }
    public String getFechaHora()    { return fechaHora; }
    public String getEstado()       { return estado; }
    public String getAutor()        { return autor; }
}