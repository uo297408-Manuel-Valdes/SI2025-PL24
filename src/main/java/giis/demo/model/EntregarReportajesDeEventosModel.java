package giis.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class EntregarReportajesDeEventosModel {

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
				(String)  r[2], "",
				(String)  r[3]
			));
		}
		return res;
	}

	// ── Eventos ───────────────────────────────────────────────────────────

	/**
	 * Devuelve los eventos asignados al reportero que aun NO han sido finalizados
	 * por el reportero responsable.
	 * Un evento esta finalizado cuando su responsable tiene es_finalizacion=1
	 * en comentario_revision para el reportaje del evento.
	 */
	public List<EventoDTO> getEventosAsignadosAReportero(int idReportero, boolean conReportaje) {
		String condicion = conReportaje
			? "AND EXISTS     (SELECT 1 FROM REPORTAJE rep WHERE rep.id_evento = e.id_evento)"
			: "AND NOT EXISTS (SELECT 1 FROM REPORTAJE rep WHERE rep.id_evento = e.id_evento)";

		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_inicio " +
			"FROM EVENTO e " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_evento = e.id_evento " +
			"WHERE ar.id_reportero = ? " +
			condicion + " " +
			"AND NOT EXISTS ( " +
			"  SELECT 1 FROM REPORTAJE r2 " +
			"  JOIN COMENTARIO_REVISION cr ON cr.id_reportaje = r2.id_reportaje " +
			"  JOIN ASIGNACION_REPORTERO ar2 ON ar2.id_reportero = cr.id_reportero " +
			"    AND ar2.id_evento = e.id_evento " +
			"  WHERE r2.id_evento = e.id_evento " +
			"  AND ar2.es_responsable = 1 " +
			"  AND cr.es_finalizacion = 1 " +
			") " +
			"ORDER BY e.fecha_inicio, e.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idReportero);
		List<EventoDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new EventoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String)  r[2],
				(String)  r[3]
			));
		}
		return res;
	}

	// ── Reportaje ─────────────────────────────────────────────────────────

	public ReportajeDTO getReportaje(int idEvento) {
		String sql =
			"SELECT id_reportaje, id_evento, titulo, id_reportero_entrega " +
			"FROM REPORTAJE WHERE id_evento = ?";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		if (rows.isEmpty()) return null;
		Object[] r = rows.get(0);
		return new ReportajeDTO(
			((Number) r[0]).intValue(),
			((Number) r[1]).intValue(),
			(String)  r[2],
			((Number) r[3]).intValue()
		);
	}

	public VersionReportajeDTO getUltimaVersion(int idReportaje) {
		String sql =
			"SELECT id_version, id_reportaje, subtitulo, cuerpo, cambios " +
			"FROM VERSION_REPORTAJE " +
			"WHERE id_reportaje = ? ORDER BY id_version DESC LIMIT 1";
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

	public boolean reporteroPuedeModificar(int idEvento, int idReportero) {
		String sql = "SELECT 1 FROM REPORTAJE WHERE id_evento = ? AND id_reportero_entrega = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento, idReportero).isEmpty();
	}

	public void validarTitulo(String titulo, int idReportajeExcluido) {
		if (titulo == null || titulo.trim().isEmpty())
			throw new ApplicationException("El titulo no puede estar vacio.");
		if (tituloExiste(titulo.trim(), idReportajeExcluido))
			throw new ApplicationException("Ya existe otro reportaje con ese titulo.");
	}

	// ── Entrega y modificacion ────────────────────────────────────────────

	public void entregarReportaje(int idEvento, int idReportero,
	                               String titulo, String subtitulo, String cuerpo) {
		if (titulo    == null || titulo.trim().isEmpty())
			throw new ApplicationException("El campo Titulo no puede estar vacio.");
		if (subtitulo == null || subtitulo.trim().isEmpty())
			throw new ApplicationException("El campo Subtitulo no puede estar vacio.");
		if (cuerpo    == null || cuerpo.trim().isEmpty())
			throw new ApplicationException("El campo Cuerpo no puede estar vacio.");

		String checkAsig = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? AND id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idEvento, idReportero).isEmpty())
			throw new ApplicationException("El reportero no tiene asignacion para este evento.");

		String tituloTrim    = titulo.trim();
		String subtituloTrim = subtitulo.trim();
		String cuerpoTrim    = cuerpo.trim();

		ReportajeDTO reportaje = getReportaje(idEvento);

		if (reportaje == null) {
			if (tituloExiste(tituloTrim, -1))
				throw new ApplicationException("Ya existe otro reportaje con ese titulo.");
			db.executeUpdate(
				"INSERT INTO REPORTAJE(id_evento, titulo, id_reportero_entrega) VALUES (?, ?, ?)",
				idEvento, tituloTrim, idReportero
			);
			reportaje = getReportaje(idEvento);
		} else {
			if (!reporteroPuedeModificar(idEvento, idReportero))
				throw new ApplicationException(
					"Solo el reportero que hizo la entrega original puede modificar el reportaje.");
			if (isPendienteRevision(reportaje.getIdReportaje()))
				throw new ApplicationException(
					"El reportaje esta pendiente de revision. No puede modificarse hasta que finalice.");
		}

		String cambiosAuto = generarCambios(reportaje, subtituloTrim, cuerpoTrim);
		db.executeUpdate(
			"INSERT INTO VERSION_REPORTAJE(id_reportaje, subtitulo, cuerpo, cambios) VALUES (?, ?, ?, ?)",
			reportaje.getIdReportaje(), subtituloTrim, cuerpoTrim, cambiosAuto
		);
	}

	/**
	 * Guarda cambios con privilegios de responsable (titulo editable).
	 */
	public void guardarVersionPrivilegiada(int idEvento, int idReportero,
	                                        String titulo, String subtitulo, String cuerpo) {
		if (titulo    == null || titulo.trim().isEmpty())
			throw new ApplicationException("El campo Titulo no puede estar vacio.");
		if (subtitulo == null || subtitulo.trim().isEmpty())
			throw new ApplicationException("El campo Subtitulo no puede estar vacio.");
		if (cuerpo    == null || cuerpo.trim().isEmpty())
			throw new ApplicationException("El campo Cuerpo no puede estar vacio.");

		ReportajeDTO reportaje = getReportaje(idEvento);
		if (reportaje == null)
			throw new ApplicationException("No existe reportaje para este evento.");

		String tituloTrim    = titulo.trim();
		String subtituloTrim = subtitulo.trim();
		String cuerpoTrim    = cuerpo.trim();

		if (tituloExiste(tituloTrim, reportaje.getIdReportaje()))
			throw new ApplicationException("Ya existe otro reportaje con ese titulo.");

		db.executeUpdate("UPDATE REPORTAJE SET titulo = ? WHERE id_reportaje = ?",
			tituloTrim, reportaje.getIdReportaje());

		String cambios = generarCambiosPrivilegiado(reportaje, subtituloTrim, cuerpoTrim, tituloTrim);
		db.executeUpdate(
			"INSERT INTO VERSION_REPORTAJE(id_reportaje, subtitulo, cuerpo, cambios) VALUES (?, ?, ?, ?)",
			reportaje.getIdReportaje(), subtituloTrim, cuerpoTrim, cambios
		);
	}

	// ── Multimedia ────────────────────────────────────────────────────────

	public List<MultimediaDTO> getMultimedia(int idReportaje) {
		String sql =
			"SELECT id_multimedia, id_reportaje, id_reportero, path, tipo, estado " +
			"FROM MULTIMEDIA_REPORTAJE WHERE id_reportaje = ? ORDER BY id_multimedia";
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

	public void addMultimedia(int idReportaje, int idReportero, int idEvento,
	                           String path, String tipo) {
		if (path == null || path.trim().isEmpty())
			throw new ApplicationException("El path no puede estar vacio.");
		if (tipo == null || (!tipo.equals("IMAGEN") && !tipo.equals("VIDEO")))
			throw new ApplicationException("El tipo debe ser IMAGEN o VIDEO.");

		String checkAsig = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? AND id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idEvento, idReportero).isEmpty())
			throw new ApplicationException("El reportero no tiene asignacion para este evento.");

		String checkPath = "SELECT 1 FROM MULTIMEDIA_REPORTAJE WHERE path = ? LIMIT 1";
		if (!db.executeQueryArray(checkPath, path.trim()).isEmpty())
			throw new ApplicationException("El path introducido ya existe en el sistema.");

		db.executeUpdate(
			"INSERT INTO MULTIMEDIA_REPORTAJE(id_reportaje, id_reportero, path, tipo, estado) VALUES (?, ?, ?, ?, ?)",
			idReportaje, idReportero, path.trim(), tipo, "BORRADOR"
		);
	}

	public void removeMultimedia(int idMultimedia, int idReportero) {
		String sql = "SELECT estado, id_reportero FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ? LIMIT 1";
		List<Object[]> rows = db.executeQueryArray(sql, idMultimedia);
		if (rows.isEmpty()) throw new ApplicationException("El elemento multimedia no existe.");

		String estado      = (String)  rows.get(0)[0];
		int    autorSubida = ((Number) rows.get(0)[1]).intValue();

		if (!estado.equals("BORRADOR"))
			throw new ApplicationException("Solo se pueden eliminar elementos en estado BORRADOR.");
		if (autorSubida != idReportero)
			throw new ApplicationException("Solo el reportero que subio el contenido puede eliminarlo.");

		db.executeUpdate("DELETE FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ?", idMultimedia);
	}

	public void removeMultimediaPrivilegiado(int idMultimedia) {
		String sql = "SELECT 1 FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ? LIMIT 1";
		if (db.executeQueryArray(sql, idMultimedia).isEmpty())
			throw new ApplicationException("El elemento multimedia no existe.");
		db.executeUpdate("DELETE FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ?", idMultimedia);
	}

	public void cambiarEstadoMultimedia(int idMultimedia, int idReportero, String nuevoEstado) {
		if (!nuevoEstado.equals("BORRADOR") && !nuevoEstado.equals("DEFINITIVO"))
			throw new ApplicationException("Estado no valido.");

		String sql = "SELECT estado, id_reportero FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ? LIMIT 1";
		List<Object[]> rows = db.executeQueryArray(sql, idMultimedia);
		if (rows.isEmpty()) throw new ApplicationException("El elemento multimedia no existe.");

		String estadoActual = (String)  rows.get(0)[0];
		int    autorSubida  = ((Number) rows.get(0)[1]).intValue();

		if (!estadoActual.equals("BORRADOR"))
			throw new ApplicationException("Solo se puede cambiar el estado de elementos en BORRADOR.");
		if (autorSubida != idReportero)
			throw new ApplicationException("Solo el reportero que subio el contenido puede cambiar su estado.");

		db.executeUpdate("UPDATE MULTIMEDIA_REPORTAJE SET estado = ? WHERE id_multimedia = ?",
			nuevoEstado, idMultimedia);
	}

	// ── Revision ──────────────────────────────────────────────────────────

	/**
	 * Un reportaje esta pendiente de revision si:
	 *   - Tiene al menos una solicitud (es_finalizacion=0)
	 *   - Algun reportero asignado aun NO ha finalizado (es_finalizacion=1)
	 *   - El responsable NO ha marcado el reportaje como finalizado todavia
	 */
	public boolean isPendienteRevision(int idReportaje) {
		// Hay solicitud de revision
		String checkSolicitud =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND es_finalizacion = 0 LIMIT 1";
		if (db.executeQueryArray(checkSolicitud, idReportaje).isEmpty()) return false;

		// El responsable ya finalizo → ya no esta pendiente
		if (estaFinalizadoPorResponsableDeReportaje(idReportaje)) return false;

		// Algun reportero asignado aun no ha finalizado su revision
		String checkPendiente =
			"SELECT 1 FROM ASIGNACION_REPORTERO ar " +
			"JOIN REPORTAJE r ON r.id_evento = ar.id_evento " +
			"WHERE r.id_reportaje = ? " +
			"AND ar.es_responsable = 0 " +  // solo los no responsables
			"AND NOT EXISTS ( " +
			"  SELECT 1 FROM COMENTARIO_REVISION cr " +
			"  WHERE cr.id_reportaje = ? " +
			"  AND   cr.id_reportero = ar.id_reportero " +
			"  AND   cr.es_finalizacion = 1 " +
			") LIMIT 1";
		return !db.executeQueryArray(checkPendiente, idReportaje, idReportaje).isEmpty();
	}

	/**
	 * Comprueba si el responsable del evento al que pertenece el reportaje
	 * ha insertado su es_finalizacion=1 (ha marcado el reportaje como finalizado).
	 */
	public boolean estaFinalizadoPorResponsableDeReportaje(int idReportaje) {
		String sql =
			"SELECT 1 FROM COMENTARIO_REVISION cr " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_reportero = cr.id_reportero " +
			"JOIN REPORTAJE r ON r.id_reportaje = cr.id_reportaje " +
			"  AND ar.id_evento = r.id_evento " +
			"WHERE cr.id_reportaje = ? " +
			"AND ar.es_responsable = 1 " +
			"AND cr.es_finalizacion = 1 LIMIT 1";
		return !db.executeQueryArray(sql, idReportaje).isEmpty();
	}

	// Alias para compatibilidad con el controller
	public boolean estaFinalizadoPorResponsable(int idReportaje, int idReportero) {
		String sql =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND id_reportero = ? AND es_finalizacion = 1 LIMIT 1";
		return !db.executeQueryArray(sql, idReportaje, idReportero).isEmpty();
	}

	/**
	 * Devuelve true si TODOS los reporteros NO responsables han finalizado su revision.
	 */
	public boolean todosHanFinalizadoRevision(int idEvento) {
		ReportajeDTO reportaje = getReportaje(idEvento);
		if (reportaje == null) return false;

		String sqlAsignados =
			"SELECT id_reportero FROM ASIGNACION_REPORTERO " +
			"WHERE id_evento = ? AND es_responsable = 0";
		List<Object[]> asignados = db.executeQueryArray(sqlAsignados, idEvento);
		if (asignados.isEmpty()) return true;

		for (Object[] row : asignados) {
			int idRep = ((Number) row[0]).intValue();
			String sqlFin =
				"SELECT 1 FROM COMENTARIO_REVISION " +
				"WHERE id_reportaje = ? AND id_reportero = ? AND es_finalizacion = 1 LIMIT 1";
			if (db.executeQueryArray(sqlFin, reportaje.getIdReportaje(), idRep).isEmpty())
				return false;
		}
		return true;
	}

	// Alias para compatibilidad
	public boolean todosHanEnviadoRevision(int idEvento) {
		return todosHanFinalizadoRevision(idEvento);
	}

	public boolean esResponsableDeEvento(int idEvento, int idReportero) {
		String sql =
			"SELECT 1 FROM ASIGNACION_REPORTERO " +
			"WHERE id_evento = ? AND id_reportero = ? AND es_responsable = 1 LIMIT 1";
		return !db.executeQueryArray(sql, idEvento, idReportero).isEmpty();
	}

	public List<ComentarioRevisionDTO> getComentariosRevision(int idReportaje) {
		String sql =
			"SELECT cr.id_comentario, cr.id_reportaje, cr.id_reportero, " +
			"       cr.comentario, cr.fecha_hora, cr.es_finalizacion, " +
			"       r.nombre AS nombre_reportero " +
			"FROM COMENTARIO_REVISION cr " +
			"JOIN REPORTERO r ON r.id_reportero = cr.id_reportero " +
			"WHERE cr.id_reportaje = ? " +
			"ORDER BY cr.id_comentario";
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		List<ComentarioRevisionDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int    esFinalizacion = ((Number) r[5]).intValue();
			String estado         = esFinalizacion == 1 ? "Finalizada" : "Sin finalizar";
			String autor          = (String) r[6];
			res.add(new ComentarioRevisionDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				((Number) r[2]).intValue(),
				(String)  r[3],
				(String)  r[4],
				estado,
				autor
			));
		}
		return res;
	}

	public void solicitarRevision(int idEvento, int idReportero) {
		ReportajeDTO reportaje = getReportaje(idEvento);
		if (reportaje == null)
			throw new ApplicationException("No existe reportaje para este evento.");
		if (!reporteroPuedeModificar(idEvento, idReportero))
			throw new ApplicationException(
				"Solo el reportero que hizo la entrega puede solicitar la revision.");
		if (isPendienteRevision(reportaje.getIdReportaje()))
			throw new ApplicationException("El reportaje ya esta pendiente de revision.");

		String fechaHora = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		db.executeUpdate(
			"INSERT INTO COMENTARIO_REVISION(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
			"VALUES (?, ?, ?, ?, 0)",
			reportaje.getIdReportaje(), idReportero, "Solicitud de revision", fechaHora
		);
	}

	/**
	 * El responsable FINALIZA el reportaje insertando es_finalizacion=1 para él.
	 * Esta accion:
	 *   - No toca el campo evento.finalizada (usado por otros compañeros)
	 *   - Requiere que no haya revision pendiente (o que no se haya solicitado ninguna)
	 *   - Tras esto el evento desaparece de la lista de trabajo de todos los reporteros
	 */
	public void finalizarReportajeResponsable(int idEvento, int idReportero) {
		if (!esResponsableDeEvento(idEvento, idReportero))
			throw new ApplicationException("Solo el reportero responsable puede finalizar el reportaje.");

		ReportajeDTO reportaje = getReportaje(idEvento);
		if (reportaje == null)
			throw new ApplicationException("No existe reportaje para este evento.");

		// Si hay revision solicitada, todos los no responsables deben haberla finalizado
		String checkSolicitud =
			"SELECT 1 FROM COMENTARIO_REVISION " +
			"WHERE id_reportaje = ? AND es_finalizacion = 0 LIMIT 1";
		boolean hayRevision = !db.executeQueryArray(checkSolicitud, reportaje.getIdReportaje()).isEmpty();

		if (hayRevision && !todosHanFinalizadoRevision(idEvento))
			throw new ApplicationException(
				"No todos los reporteros han finalizado su revision. " +
				"No es posible finalizar el reportaje hasta que todos completen la revision.");

		// Verificar que el responsable no ha finalizado ya
		if (estaFinalizadoPorResponsable(reportaje.getIdReportaje(), idReportero))
			throw new ApplicationException("El reportaje ya ha sido finalizado por el responsable.");

		String fechaHora = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		// Insertar es_finalizacion=1 para el responsable — marca el reportaje como finalizado
		db.executeUpdate(
			"INSERT INTO COMENTARIO_REVISION(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
			"VALUES (?, ?, ?, ?, 1)",
			reportaje.getIdReportaje(), idReportero, "Reportaje finalizado por responsable", fechaHora
		);
	}

	// ── Helpers privados ──────────────────────────────────────────────────

	private String generarCambios(ReportajeDTO reportaje, String nuevoSubtitulo, String nuevoCuerpo) {
		String ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		VersionReportajeDTO ultima = getUltimaVersion(reportaje.getIdReportaje());
		if (ultima == null) return "Primera entrega. " + ahora;
		List<String> cambiosList = new ArrayList<>();
		if (!ultima.getSubtitulo().equals(nuevoSubtitulo)) cambiosList.add("subtitulo");
		if (!ultima.getCuerpo().equals(nuevoCuerpo))       cambiosList.add("cuerpo");
		if (cambiosList.isEmpty()) return "Sin cambios. " + ahora;
		return "Modificado: " + String.join(" y ", cambiosList) + ". " + ahora;
	}

	private String generarCambiosPrivilegiado(ReportajeDTO reportaje, String nuevoSubtitulo,
	                                           String nuevoCuerpo, String nuevoTitulo) {
		String ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		VersionReportajeDTO ultima = getUltimaVersion(reportaje.getIdReportaje());
		List<String> cambiosList = new ArrayList<>();
		if (!reportaje.getTitulo().equals(nuevoTitulo)) cambiosList.add("titulo");
		if (ultima != null) {
			if (!ultima.getSubtitulo().equals(nuevoSubtitulo)) cambiosList.add("subtitulo");
			if (!ultima.getCuerpo().equals(nuevoCuerpo))       cambiosList.add("cuerpo");
		} else {
			cambiosList.add("subtitulo");
			cambiosList.add("cuerpo");
		}
		if (cambiosList.isEmpty()) return "Sin cambios (responsable). " + ahora;
		return "Modificado por responsable: " + String.join(", ", cambiosList) + ". " + ahora;
	}

	private boolean tituloExiste(String titulo, int idReportajePropio) {
		String sql = "SELECT 1 FROM REPORTAJE WHERE titulo = ? AND id_reportaje != ? LIMIT 1";
		return !db.executeQueryArray(sql, titulo, idReportajePropio).isEmpty();
	}
}