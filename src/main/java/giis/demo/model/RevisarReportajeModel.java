package giis.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class RevisarReportajeModel {

	private final Database db = new Database();

	// ── Reporteros ────────────────────────────────────────────────────────

	public List<ReporteroDTO> getReporteros() {
		String sql = "SELECT id_reportero, id_agencia, nombre, tipo_reportero FROM REPORTERO ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);
		List<ReporteroDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String)  r[2],
				"",
				(String)  r[3]
			));
		}
		return res;
	}

	// ── Reportajes pendientes ─────────────────────────────────────────────

	/**
	 * Devuelve los reportajes pendientes de revision a los que el reportero esta asignado.
	 * Un reportaje esta pendiente si tiene al menos un comentario de solicitud (es_finalizacion=0)
	 * y este reportero no ha finalizado todavia su revision.
	 */
	public List<ReportajeDTO> getReportajesPendientesDeRevision(int idReportero) {
		String sql =
			"SELECT DISTINCT r.id_reportaje, r.id_evento, r.titulo, r.id_reportero_entrega " +
			"FROM REPORTAJE r " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_evento = r.id_evento " +
			"JOIN COMENTARIO_REVISION cr  ON cr.id_reportaje = r.id_reportaje " +
			"WHERE ar.id_reportero = ? " +
			"AND cr.es_finalizacion = 0 " +
			"AND NOT EXISTS ( " +
			"  SELECT 1 FROM COMENTARIO_REVISION cr2 " +
			"  WHERE cr2.id_reportaje = r.id_reportaje " +
			"  AND cr2.id_reportero  = ? " +
			"  AND cr2.es_finalizacion = 1 " +
			") " +
			"ORDER BY r.titulo";
		List<Object[]> rows = db.executeQueryArray(sql, idReportero, idReportero);
		List<ReportajeDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new ReportajeDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String)  r[2],
				((Number) r[3]).intValue()
			));
		}
		return res;
	}

	// ── Contenido del reportaje ───────────────────────────────────────────

	public VersionReportajeDTO getUltimaVersion(int idReportaje) {
		String sql =
			"SELECT id_version, id_reportaje, subtitulo, cuerpo, cambios " +
			"FROM VERSION_REPORTAJE " +
			"WHERE id_reportaje = ? " +
			"ORDER BY id_version DESC LIMIT 1";
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		if (rows.isEmpty()) return null;
		Object[] r = rows.get(0);
		return new VersionReportajeDTO(
			((Number) r[0]).intValue(),
			((Number) r[1]).intValue(),
			(String)  r[2],
			(String)  r[3],
			(String)  r[4]
		);
	}

	/**
	 * Devuelve TODO el multimedia del reportaje (borrador y definitivo).
	 */
	public List<MultimediaDTO> getMultimedia(int idReportaje) {
		String sql =
			"SELECT id_multimedia, id_reportaje, id_reportero, path, tipo, estado " +
			"FROM MULTIMEDIA_REPORTAJE " +
			"WHERE id_reportaje = ? " +
			"ORDER BY id_multimedia";
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		List<MultimediaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new MultimediaDTO(
				((Number) r[0]).intValue(),
				((Number) r[2]).intValue(),
				((Number) r[1]).intValue(),
				(String)  r[4],
				(String)  r[5],
				(String)  r[3]
			));
		}
		return res;
	}

	// ── Comentarios ───────────────────────────────────────────────────────

	/**
	 * Devuelve los comentarios de revision (excluye los de finalizacion) con el nombre del autor.
	 */
	public List<ComentarioRevisionDTO> getComentariosRevision(int idReportaje) {
		String sql =
			"SELECT cr.id_comentario, cr.id_reportaje, cr.id_reportero, " +
			"       cr.comentario, cr.fecha_hora, cr.es_finalizacion, rep.nombre " +
			"FROM COMENTARIO_REVISION cr " +
			"JOIN REPORTERO rep ON rep.id_reportero = cr.id_reportero " +
			"WHERE cr.id_reportaje = ? AND cr.es_finalizacion = 0 " +
			"ORDER BY cr.id_comentario";
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		List<ComentarioRevisionDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			String estadoStr   = "PENDIENTE";
			String nombreAutor = (String) r[6];
			res.add(new ComentarioRevisionDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				((Number) r[2]).intValue(),
				(String)  r[3],
				(String)  r[4],
				estadoStr,
				nombreAutor
			));
		}
		return res;
	}

	/**
	 * Anade un comentario de revision al reportaje.
	 * Solo se puede comentar si el reportero aun no ha finalizado su revision.
	 */
	public void addComentarioRevision(int idReportaje, int idReportero, String comentario) {
		if (comentario == null || comentario.trim().isEmpty())
			throw new ApplicationException("El comentario no puede estar vacio.");

		// Verificar que el reportero esta asignado al evento del reportaje
		String checkAsig =
			"SELECT 1 FROM ASIGNACION_REPORTERO ar " +
			"JOIN REPORTAJE r ON r.id_evento = ar.id_evento " +
			"WHERE r.id_reportaje = ? AND ar.id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idReportaje, idReportero).isEmpty())
			throw new ApplicationException("El reportero no esta asignado al evento de este reportaje.");

		// Verificar que este reportero no ha finalizado ya su revision
		if (haFinalizadoRevision(idReportaje, idReportero))
			throw new ApplicationException("Ya has finalizado tu revision de este reportaje. No puedes anadir mas comentarios.");

		String fechaHora = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		db.executeUpdate(
			"INSERT INTO COMENTARIO_REVISION(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
			"VALUES (?, ?, ?, ?, 0)",
			idReportaje, idReportero, comentario.trim(), fechaHora
		);
	}

	// ── Finalizacion individual por reportero ─────────────────────────────

	/**
	 * Comprueba si este reportero ya ha finalizado su revision de este reportaje.
	 */
	public boolean haFinalizadoRevision(int idReportaje, int idReportero) {
		String sql =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND id_reportero = ? AND es_finalizacion = 1 LIMIT 1";
		return !db.executeQueryArray(sql, idReportaje, idReportero).isEmpty();
	}

	/**
	 * Devuelve true si TODOS los reporteros asignados al evento han finalizado su revision.
	 */
	public boolean todosHanFinalizadoRevision(int idReportaje) {
		// Obtener id_evento del reportaje
		String sqlEvento = "SELECT id_evento FROM REPORTAJE WHERE id_reportaje = ? LIMIT 1";
		List<Object[]> rowsEvento = db.executeQueryArray(sqlEvento, idReportaje);
		if (rowsEvento.isEmpty()) return false;
		int idEvento = ((Number) rowsEvento.get(0)[0]).intValue();

		// Obtener todos los reporteros asignados al evento
		String sqlAsignados = "SELECT id_reportero FROM ASIGNACION_REPORTERO WHERE id_evento = ?";
		List<Object[]> asignados = db.executeQueryArray(sqlAsignados, idEvento);
		if (asignados.isEmpty()) return false;

		// Verificar que cada uno ha finalizado
		for (Object[] row : asignados) {
			int idRep = ((Number) row[0]).intValue();
			if (!haFinalizadoRevision(idReportaje, idRep)) return false;
		}
		return true;
	}

	/**
	 * Finaliza la revision de ESTE reportero para este reportaje.
	 * Cada reportero finaliza de forma independiente.
	 * Solo se puede finalizar si hay al menos un comentario previo de este reportero.
	 */
	public void finalizarRevision(int idReportaje, int idReportero) {
		// Verificar que el reportero esta asignado al evento
		String checkAsig =
			"SELECT 1 FROM ASIGNACION_REPORTERO ar " +
			"JOIN REPORTAJE r ON r.id_evento = ar.id_evento " +
			"WHERE r.id_reportaje = ? AND ar.id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idReportaje, idReportero).isEmpty())
			throw new ApplicationException("El reportero no esta asignado al evento de este reportaje.");

		// Verificar que el reportero no ha finalizado ya
		if (haFinalizadoRevision(idReportaje, idReportero))
			throw new ApplicationException("Ya has finalizado tu revision de este reportaje.");

		// Verificar que el reportero tiene al menos un comentario
		String checkComentario =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND id_reportero = ? AND es_finalizacion = 0 LIMIT 1";
		if (db.executeQueryArray(checkComentario, idReportaje, idReportero).isEmpty())
			throw new ApplicationException("Debes anadir al menos un comentario antes de finalizar tu revision.");

		String fechaHora = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		db.executeUpdate(
			"INSERT INTO COMENTARIO_REVISION(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
			"VALUES (?, ?, ?, ?, 1)",
			idReportaje, idReportero, "Revision finalizada", fechaHora
		);
	}
}