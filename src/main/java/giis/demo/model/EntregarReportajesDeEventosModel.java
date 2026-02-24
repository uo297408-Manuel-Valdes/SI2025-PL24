package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class EntregarReportajesDeEventosModel {

    private final Database db = new Database();

    /**
     * Obtiene los eventos asignados al reportero dado (los que tienen asignación).
     */
    public List<EventoDTO> getEventosAsignadosAReportero(int idReportero) {
        String sql =
            "SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
            "FROM EVENTO e " +
            "JOIN ASIGNACION_REPORTERO ar ON ar.id_evento = e.id_evento " +
            "WHERE ar.id_reportero = ? " +
            "ORDER BY e.fecha_evento, e.nombre";

        List<Object[]> rows = db.executeQueryArray(sql, idReportero);
        List<EventoDTO> res = new ArrayList<>();
        for (Object[] r : rows) {
            int idEvento   = ((Number) r[0]).intValue();
            int idAgencia  = ((Number) r[1]).intValue();
            String nombre  = (String) r[2];
            String fecha   = (String) r[3];
            res.add(new EventoDTO(idEvento, idAgencia, nombre, fecha));
        }
        return res;
    }

    /**
     * Obtiene el reportaje existente para un evento y reportero, o null si no existe.
     */
    public ReportajeDTO getReportaje(int idEvento, int idReportero) {
        String sql =
            "SELECT id_reportaje, id_evento, id_reportero, autor, titulo, subtitulo, cuerpo " +
            "FROM REPORTAJE " +
            "WHERE id_evento = ? AND id_reportero = ?";

        List<Object[]> rows = db.executeQueryArray(sql, idEvento, idReportero);
        if (rows.isEmpty()) return null;

        Object[] r = rows.get(0);
        return new ReportajeDTO(
            ((Number) r[0]).intValue(),
            ((Number) r[1]).intValue(),
            ((Number) r[2]).intValue(),
            (String) r[3],
            (String) r[4],
            (String) r[5],
            (String) r[6]
        );
    }

    /**
     * Valida y guarda (inserta o actualiza) el reportaje.
     */
    public void entregarReportaje(int idEvento, int idReportero,
                                   String autor, String titulo,
                                   String subtitulo, String cuerpo) {

        if (autor == null || autor.trim().isEmpty())
            throw new ApplicationException("El campo 'Autor' no puede estar vacío.");
        if (titulo == null || titulo.trim().isEmpty())
            throw new ApplicationException("El campo 'Título' no puede estar vacío.");
        if (subtitulo == null || subtitulo.trim().isEmpty())
            throw new ApplicationException("El campo 'Subtítulo' no puede estar vacío.");
        if (cuerpo == null || cuerpo.trim().isEmpty())
            throw new ApplicationException("El campo 'Cuerpo' no puede estar vacío.");

        // Verificar que la asignación existe
        String checkSql =
            "SELECT 1 FROM ASIGNACION_REPORTERO " +
            "WHERE id_evento = ? AND id_reportero = ? LIMIT 1";
        if (db.executeQueryArray(checkSql, idEvento, idReportero).isEmpty())
            throw new ApplicationException("No existe asignación para este evento y reportero.");

        // Insertar o actualizar
        ReportajeDTO existing = getReportaje(idEvento, idReportero);
        if (existing == null) {
            String insert =
                "INSERT INTO REPORTAJE(id_evento, id_reportero, autor, titulo, subtitulo, cuerpo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
            db.executeUpdate(insert, idEvento, idReportero,
                             autor.trim(), titulo.trim(), subtitulo.trim(), cuerpo.trim());
        } else {
            String update =
                "UPDATE REPORTAJE SET autor=?, titulo=?, subtitulo=?, cuerpo=? " +
                "WHERE id_evento=? AND id_reportero=?";
            db.executeUpdate(update,
                             autor.trim(), titulo.trim(), subtitulo.trim(), cuerpo.trim(),
                             idEvento, idReportero);
        }
    }

    /**
     * Valida el título sin guardar (para el botón de check en tiempo real).
     * Lanza ApplicationException si está vacío.
     */
    public void validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty())
            throw new ApplicationException("El título no puede estar vacío.");
    }
}