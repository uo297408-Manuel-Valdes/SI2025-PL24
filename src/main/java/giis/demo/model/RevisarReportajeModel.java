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
	 * Un reportaje esta pendiente si:
	 *   - Tiene al menos un comentario de revision (es_finalizacion = 0)
	 *   - No tiene ningun comentario de finalizacion (es_finalizacion = 1)
	 */
	public List<ReportajeDTO> getReportajesPendientesDeRevision(int idReportero) {
		String sql =
			"SELECT DISTINCT r.id_reportaje, r.id_evento, r.titulo, r.id_reportero_entrega " +
			"FROM REPORTAJE r " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_evento = r.id_evento " +
			"JOIN COMENTARIO_REVISION cr  ON cr.id_reportaje = r.id_reportaje " +
			"WHERE ar.id_reportero = ? " +
			"AND NOT EXISTS ( " +
			"  SELECT 1 FROM COMENTARIO_REVISION cr2 " +
			"  WHERE cr2.id_reportaje = r.id_reportaje AND cr2.es_finalizacion = 1 " +
			") " +
			"ORDER BY r.titulo";
		List<Object[]> rows = db.executeQueryArray(sql, idReportero);
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
	 * Devuelve los comentarios de revision (excluye el de finalizacion).
	 */
	public List<ComentarioRevisionDTO> getComentariosRevision(int idReportaje) {
		String sql =
			"SELECT id_comentario, id_reportaje, id_reportero, comentario, fecha_hora " +
			"FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND es_finalizacion = 0 " +
			"ORDER BY id_comentario";
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		List<ComentarioRevisionDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new ComentarioRevisionDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				((Number) r[2]).intValue(),
				(String)  r[3],
				(String)  r[4]
			));
		}
		return res;
	}

	/**
	 * Anade un comentario de revision al reportaje.
	 * Cualquier reportero asignado al evento puede comentar.
	 * No se puede comentar si la revision ya esta finalizada.
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

		// Verificar que la revision no esta finalizada
		String checkFin =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND es_finalizacion = 1 LIMIT 1";
		if (!db.executeQueryArray(checkFin, idReportaje).isEmpty())
			throw new ApplicationException("La revision ya ha sido finalizada.");

		String fechaHora = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		db.executeUpdate(
			"INSERT INTO COMENTARIO_REVISION(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
			"VALUES (?, ?, ?, ?, 0)",
			idReportaje, idReportero, comentario.trim(), fechaHora
		);
	}

	// ── Finalizar revision ────────────────────────────────────────────────

	/**
	 * Finaliza la revision insertando un comentario con es_finalizacion = 1.
	 * Cualquier reportero asignado puede finalizarla.
	 * Solo se puede finalizar si hay revision pendiente y no esta ya finalizada.
	 */
	public void finalizarRevision(int idReportaje, int idReportero) {
		// Verificar que el reportero esta asignado al evento
		String checkAsig =
			"SELECT 1 FROM ASIGNACION_REPORTERO ar " +
			"JOIN REPORTAJE r ON r.id_evento = ar.id_evento " +
			"WHERE r.id_reportaje = ? AND ar.id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idReportaje, idReportero).isEmpty())
			throw new ApplicationException("El reportero no esta asignado al evento de este reportaje.");

		// Verificar que hay revision pendiente
		String checkPend =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND es_finalizacion = 0 LIMIT 1";
		if (db.executeQueryArray(checkPend, idReportaje).isEmpty())
			throw new ApplicationException("Este reportaje no tiene ninguna revision pendiente.");

		// Verificar que no esta ya finalizada
		String checkFin =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND es_finalizacion = 1 LIMIT 1";
		if (!db.executeQueryArray(checkFin, idReportaje).isEmpty())
			throw new ApplicationException("La revision ya ha sido finalizada anteriormente.");

		String fechaHora = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		db.executeUpdate(
			"INSERT INTO COMENTARIO_REVISION(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
			"VALUES (?, ?, ?, ?, 1)",
			idReportaje, idReportero, "Revision finalizada", fechaHora
		);
	}
}