package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class EntregarReportajesDeEventosModel {

	private final Database db = new Database();


	/**
	 * Devuelve los eventos que tienen al menos una asignación del reportero dado.
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
			int    idEvento  = ((Number) r[0]).intValue();
			int    idAgencia = ((Number) r[1]).intValue();
			String nombre    = (String) r[2];
			String fecha     = (String) r[3];
			res.add(new EventoDTO(idEvento, idAgencia, nombre, fecha));
		}
		return res;
	}

	/**
	 * Devuelve el nombre del reportero para mostrarlo como "Autor" (campo de solo lectura).
	 */
	public String getNombreReportero(int idReportero) {
		String sql = "SELECT nombre FROM REPORTERO WHERE id_reportero = ?";
		List<Object[]> rows = db.executeQueryArray(sql, idReportero);
		if (rows.isEmpty())
			throw new ApplicationException("No se encontró el reportero con id " + idReportero);
		return (String) rows.get(0)[0];
	}

	/**
	 * Devuelve el reportaje existente para un evento, o null si todavía no se ha entregado ninguno.
	 */
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

	/**
	 * Devuelve la versión más reciente del reportaje, o null si no hay ninguna.
	 */
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
	 * Valida el título sin persistir nada (para el botón ✔ de la vista).
	 */
	public void validarTitulo(String titulo) {
		if (titulo == null || titulo.trim().isEmpty())
			throw new ApplicationException("El título no puede estar vacío.");
	}

	
	/**
	 * Valida todos los campos y persiste la entrega del reportaje.
	 *
	 * Lógica:
	 *  - Si el evento aún no tiene reportaje → INSERT en REPORTAJE + INSERT en VERSION_REPORTAJE.
	 *  - Si ya existe → UPDATE del título (si cambió) + INSERT de nueva VERSION_REPORTAJE.
	 *
	 * @param idEvento    Evento para el que se entrega el reportaje.
	 * @param idReportero Reportero que entrega (será id_reportero_entrega).
	 * @param titulo      Título del reportaje (único en toda la tabla).
	 * @param subtitulo   Subtítulo de esta versión.
	 * @param cuerpo      Cuerpo de esta versión.
	 * @param cambios     Descripción de los cambios de esta versión.
	 */
	public void entregarReportaje(int idEvento, int idReportero,
	                               String titulo, String subtitulo,
	                               String cuerpo, String cambios) {
		if (titulo    == null || titulo.trim().isEmpty())
			throw new ApplicationException("El campo 'Título' no puede estar vacío.");
		if (subtitulo == null || subtitulo.trim().isEmpty())
			throw new ApplicationException("El campo 'Subtítulo' no puede estar vacío.");
		if (cuerpo    == null || cuerpo.trim().isEmpty())
			throw new ApplicationException("El campo 'Cuerpo' no puede estar vacío.");
		if (cambios   == null || cambios.trim().isEmpty())
			throw new ApplicationException("El campo 'Cambios' no puede estar vacío.");

		
		String checkAsig =
			"SELECT 1 FROM ASIGNACION_REPORTERO " +
			"WHERE id_evento = ? AND id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idEvento, idReportero).isEmpty())
			throw new ApplicationException(
				"El reportero no tiene asignación para este evento.");

		String tituloTrim    = titulo.trim();
		String subtituloTrim = subtitulo.trim();
		String cuerpoTrim    = cuerpo.trim();
		String cambiosTrim   = cambios.trim();

		ReportajeDTO reportaje = getReportaje(idEvento);

		if (reportaje == null) {
			
			if (tituloExiste(tituloTrim, -1))
				throw new ApplicationException(
					"Ya existe otro reportaje con ese título. Elige un título diferente.");

			String insertRep =
				"INSERT INTO REPORTAJE(id_evento, titulo, id_reportero_entrega) " +
				"VALUES (?, ?, ?)";
			db.executeUpdate(insertRep, idEvento, tituloTrim, idReportero);

	
			reportaje = getReportaje(idEvento);

		} else {
			
			if (!reportaje.getTitulo().equals(tituloTrim)) {
				if (tituloExiste(tituloTrim, reportaje.getIdReportaje()))
					throw new ApplicationException(
						"Ya existe otro reportaje con ese título. Elige un título diferente.");
				String updateTit =
					"UPDATE REPORTAJE SET titulo = ?, id_reportero_entrega = ? " +
					"WHERE id_reportaje = ?";
				db.executeUpdate(updateTit, tituloTrim, idReportero, reportaje.getIdReportaje());
			}
		}

		String insertVer =
			"INSERT INTO VERSION_REPORTAJE(id_reportaje, subtitulo, cuerpo, cambios) " +
			"VALUES (?, ?, ?, ?)";
		db.executeUpdate(insertVer,
			reportaje.getIdReportaje(), subtituloTrim, cuerpoTrim, cambiosTrim);
	}

	
	/**
	 * Comprueba si ya existe un reportaje con ese título, excluyendo el de idReportajePropio
	 * (para poder actualizar sin colisionar consigo mismo). Pasar -1 si es un INSERT nuevo.
	 */
	private boolean tituloExiste(String titulo, int idReportajePropio) {
		String sql =
			"SELECT 1 FROM REPORTAJE WHERE titulo = ? AND id_reportaje != ? LIMIT 1";
		return !db.executeQueryArray(sql, titulo, idReportajePropio).isEmpty();
	}
}