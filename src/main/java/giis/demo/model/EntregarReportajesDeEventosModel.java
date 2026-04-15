package giis.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class EntregarReportajesDeEventosModel {

	private final Database db = new Database();



	public List<ReporteroDTO> getReporteros() {
		String sql = "SELECT id_reportero, id_agencia, nombre, tipo_reportero  FROM REPORTERO ORDER BY nombre";
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

	public List<EventoDTO> getEventosAsignadosAReportero(int idReportero, boolean conReportaje) {
		String condicion = conReportaje
			? "AND EXISTS     (SELECT 1 FROM REPORTAJE r WHERE r.id_evento = e.id_evento)"
			: "AND NOT EXISTS (SELECT 1 FROM REPORTAJE r WHERE r.id_evento = e.id_evento)";
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_inicio " +
			"FROM EVENTO e " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_evento = e.id_evento " +
			"WHERE ar.id_reportero = ? " +
			condicion + " " +
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

	public boolean reporteroPuedeModificar(int idEvento, int idReportero) {
		String sql = "SELECT 1 FROM REPORTAJE WHERE id_evento = ? AND id_reportero_entrega = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento, idReportero).isEmpty();
	}


	
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

	
	public void addMultimedia(int idReportaje, int idReportero,int idEvento,
	                           String path, String tipo) {

		if (path == null || path.trim().isEmpty())
			throw new ApplicationException("El path no puede estar vacio.");
		if (tipo == null || (!tipo.equals("IMAGEN") && !tipo.equals("VIDEO")))
			throw new ApplicationException("El tipo debe ser IMAGEN o VIDEO.");

		// Cualquier reportero asignado al evento puede añadir multimedia
		String checkAsig = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? AND id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idEvento, idReportero).isEmpty())
			throw new ApplicationException("El reportero no tiene asignacion para este evento.");

		// El path no puede repetirse en todo el sistema
		String checkPath = "SELECT 1 FROM MULTIMEDIA_REPORTAJE WHERE path = ? LIMIT 1";
		if (!db.executeQueryArray(checkPath, path.trim()).isEmpty())
			throw new ApplicationException("El path introducido ya existe en el sistema.");

		String insert = "INSERT INTO MULTIMEDIA_REPORTAJE(id_reportaje, id_reportero, path, tipo, estado) VALUES (?, ?, ?, ?, ?)";
		db.executeUpdate(insert, idReportaje, idReportero,path.trim(), tipo, "BORRADOR");
	}

	/**
	 * Elimina un elemento multimedia por su id.
	 */
	public void removeMultimedia(int idMultimedia, int idReportero) {
	    // Verificar que existe y obtener su estado y autor
	    String sql = "SELECT estado, id_reportero FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ? LIMIT 1";
	    List<Object[]> rows = db.executeQueryArray(sql, idMultimedia);
	    if (rows.isEmpty())
	        throw new ApplicationException("El elemento multimedia no existe.");

	    String estado       = (String)  rows.get(0)[0];
	    int    autorSubida  = ((Number) rows.get(0)[1]).intValue();

	    if (!estado.equals("BORRADOR"))
	        throw new ApplicationException("Solo se pueden eliminar elementos en estado BORRADOR.");
	    if (autorSubida != idReportero)
	        throw new ApplicationException("Solo el reportero que subió el contenido puede eliminarlo.");

	    db.executeUpdate("DELETE FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ?", idMultimedia);
	}

	/**
	 * Elimina cualquier multimedia sin restriccion de estado ni de autor.
	 * Solo puede llamarse en modo privilegiado (reportero responsable).
	 */
	public void removeMultimediaPrivilegiado(int idMultimedia) {
	    String sql = "SELECT 1 FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ? LIMIT 1";
	    if (db.executeQueryArray(sql, idMultimedia).isEmpty())
	        throw new ApplicationException("El elemento multimedia no existe.");
	    db.executeUpdate("DELETE FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ?", idMultimedia);
	}

	public void cambiarEstadoMultimedia(int idMultimedia, int idReportero, String nuevoEstado) {
	    if (!nuevoEstado.equals("BORRADOR") && !nuevoEstado.equals("DEFINITIVO"))
	        throw new ApplicationException("Estado no válido.");

	    String sql = "SELECT estado, id_reportero FROM MULTIMEDIA_REPORTAJE WHERE id_multimedia = ? LIMIT 1";
	    List<Object[]> rows = db.executeQueryArray(sql, idMultimedia);
	    if (rows.isEmpty())
	        throw new ApplicationException("El elemento multimedia no existe.");

	    String estadoActual = (String)  rows.get(0)[0];
	    int    autorSubida  = ((Number) rows.get(0)[1]).intValue();

	    if (!estadoActual.equals("BORRADOR"))
	        throw new ApplicationException("Solo se puede cambiar el estado de elementos en estado BORRADOR.");
	    if (autorSubida != idReportero)
	        throw new ApplicationException("Solo el reportero que subió el contenido puede cambiar su estado.");

	    db.executeUpdate("UPDATE MULTIMEDIA_REPORTAJE SET estado = ? WHERE id_multimedia = ?",
	                     nuevoEstado, idMultimedia);
	}
	
	
	
	public void validarTitulo(String titulo, int idReportajeExcluido) {
		if (titulo == null || titulo.trim().isEmpty())
			throw new ApplicationException("El titulo no puede estar vacio.");
		if (tituloExiste(titulo.trim(), idReportajeExcluido))
			throw new ApplicationException("Ya existe otro reportaje con ese titulo.");
	}


	/**
	 * Persiste la entrega del reportaje.
	 * Los cambios (subtitulo y/o cuerpo) y la fecha/hora se generan automaticamente.
	 * En modificacion: solo puede el reportero original; el titulo no cambia.
	 */
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
			String insertRep = "INSERT INTO REPORTAJE(id_evento, titulo, id_reportero_entrega) VALUES (?, ?, ?)";
			db.executeUpdate(insertRep, idEvento, tituloTrim, idReportero);
			reportaje = getReportaje(idEvento);
		} else {
			 if (!reporteroPuedeModificar(idEvento, idReportero))
			        throw new ApplicationException(
			            "Solo el reportero que hizo la entrega original puede modificar el reportaje.");
			    if (isPendienteRevision(reportaje.getIdReportaje())) 
			        throw new ApplicationException(
			            "El reportaje esta pendiente de revision y no puede modificarse.");
		}

		String cambiosAuto = generarCambios(reportaje, subtituloTrim, cuerpoTrim);
		String insertVer = "INSERT INTO VERSION_REPORTAJE(id_reportaje, subtitulo, cuerpo, cambios) VALUES (?, ?, ?, ?)";
		db.executeUpdate(insertVer, reportaje.getIdReportaje(), subtituloTrim, cuerpoTrim, cambiosAuto);
	}

	/**
	 * Guarda una nueva version del reportaje con privilegios de responsable.
	 * El titulo puede cambiarse y no se comprueba isPendienteRevision.
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

	    // El responsable puede cambiar el titulo
	    db.executeUpdate("UPDATE REPORTAJE SET titulo = ? WHERE id_reportaje = ?",
	                     tituloTrim, reportaje.getIdReportaje());

	    String cambios = generarCambiosPrivilegiado(reportaje, subtituloTrim, cuerpoTrim, tituloTrim);
	    db.executeUpdate(
	        "INSERT INTO VERSION_REPORTAJE(id_reportaje, subtitulo, cuerpo, cambios) VALUES (?, ?, ?, ?)",
	        reportaje.getIdReportaje(), subtituloTrim, cuerpoTrim, cambios
	    );
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


	private String generarCambios(ReportajeDTO reportaje, String nuevoSubtitulo, String nuevoCuerpo) {
		String ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		VersionReportajeDTO ultima = getUltimaVersion(reportaje.getIdReportaje());
		if (ultima == null) return "Primera entrega. " + ahora;
		List<String> cambiosList = new ArrayList<>();
		if (!ultima.getSubtitulo().equals(nuevoSubtitulo)) cambiosList.add("subtitulo");
		if (!ultima.getCuerpo().equals(nuevoCuerpo)) cambiosList.add("cuerpo");
		if (cambiosList.isEmpty()) return "Sin cambios. " + ahora;
		return "Modificado: " + String.join(" y ", cambiosList) + ". " + ahora;
	}

	private boolean tituloExiste(String titulo, int idReportajePropio) {
		String sql = "SELECT 1 FROM REPORTAJE WHERE titulo = ? AND id_reportaje != ? LIMIT 1";
		return !db.executeQueryArray(sql, titulo, idReportajePropio).isEmpty();
	}
	
	
	public boolean isPendienteRevision(int idReportaje) {
	    // Pendiente = tiene al menos un comentario Y no tiene comentario de finalizacion
	    String sql =
	        "SELECT 1 FROM COMENTARIO_REVISION " +
	        "WHERE id_reportaje = ? AND es_finalizacion = 0 " +
	        "AND NOT EXISTS ( " +
	        "  SELECT 1 FROM COMENTARIO_REVISION cr2 " +
	        "  WHERE cr2.id_reportaje = ? AND cr2.es_finalizacion = 1 " +
	        ") LIMIT 1";
	    return !db.executeQueryArray(sql, idReportaje, idReportaje).isEmpty();
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

	// -------------------------------------------------------------------------
	// Metodos para el modo privilegiado del reportero responsable
	// -------------------------------------------------------------------------

	/** Devuelve true si el reportero es responsable del evento indicado. */
	public boolean esResponsableDeEvento(int idEvento, int idReportero) {
	    String sql =
	        "SELECT 1 FROM ASIGNACION_REPORTERO " +
	        "WHERE id_evento = ? AND id_reportero = ? AND es_responsable = 1 LIMIT 1";
	    return !db.executeQueryArray(sql, idEvento, idReportero).isEmpty();
	}

	/**
	 * Devuelve true si TODOS los reporteros no-responsables asignados al evento
	 * han enviado al menos un comentario de revision (solicitarRevision).
	 */
	public boolean todosHanEnviadoRevision(int idEvento) {
	    String sqlAsignados =
	        "SELECT ar.id_reportero FROM ASIGNACION_REPORTERO ar " +
	        "WHERE ar.id_evento = ? AND ar.es_responsable = 0";
	    List<Object[]> asignados = db.executeQueryArray(sqlAsignados, idEvento);
	    if (asignados.isEmpty()) return true;

	    ReportajeDTO reportaje = getReportaje(idEvento);
	    if (reportaje == null) return false;

	    for (Object[] row : asignados) {
	        int idRep = ((Number) row[0]).intValue();
	        String sqlCom =
	            "SELECT 1 FROM COMENTARIO_REVISION " +
	            "WHERE id_reportaje = ? AND id_reportero = ? LIMIT 1";
	        if (db.executeQueryArray(sqlCom, reportaje.getIdReportaje(), idRep).isEmpty())
	            return false;
	    }
	    return true;
	}

	/** Indica si el responsable ya ha finalizado este reportaje. */
	public boolean estaFinalizadoPorResponsable(int idReportaje, int idReportero) {
	    String sql =
	        "SELECT 1 FROM COMENTARIO_REVISION " +
	        "WHERE id_reportaje = ? AND id_reportero = ? AND es_finalizacion = 1 LIMIT 1";
	    return !db.executeQueryArray(sql, idReportaje, idReportero).isEmpty();
	}

	/**
	 * El responsable finaliza el reportaje: inserta un comentario con es_finalizacion = 1.
	 * Solo posible si todos los reporteros no-responsables han enviado su revision.
	 */
	public void finalizarReportajeResponsable(int idEvento, int idReportero) {
	    ReportajeDTO reportaje = getReportaje(idEvento);
	    if (reportaje == null)
	        throw new ApplicationException("No existe reportaje para este evento.");
	    if (!todosHanEnviadoRevision(idEvento))
	        throw new ApplicationException(
	            "No todos los reporteros han enviado su revision. " +
	            "El reportaje no puede finalizarse todavia.");
	    if (estaFinalizadoPorResponsable(reportaje.getIdReportaje(), idReportero))
	        throw new ApplicationException("El reportaje ya ha sido finalizado.");

	    String fechaHora = LocalDateTime.now()
	        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	    db.executeUpdate(
	        "INSERT INTO COMENTARIO_REVISION" +
	        "(id_reportaje, id_reportero, comentario, fecha_hora, es_finalizacion) " +
	        "VALUES (?, ?, ?, ?, 1)",
	        reportaje.getIdReportaje(), idReportero,
	        "Revision finalizada por responsable", fechaHora
	    );
	}
}