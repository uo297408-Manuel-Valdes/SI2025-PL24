package giis.demo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class AsignarReporterosAEventosModel {

	private final Database db = new Database();

	public List<AgenciaDTO> getAgencias() {
		String sql = "SELECT id_agencia, nombre FROM agencia_prensa ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);
		List<AgenciaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int id = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new AgenciaDTO(id, nombre));
		}
		return res;
	}

	public List<EventoDTO> getEventosSinAsignacion(int idAgencia) {
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento, " +
			"COALESCE(GROUP_CONCAT(t.nombre, ', '), '') AS tematicas " +
			"FROM evento e " +
			"LEFT JOIN evento_tematica et ON et.id_evento = e.id_evento " +
			"LEFT JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"WHERE e.id_agencia = ? " +
			"AND NOT EXISTS (SELECT 1 FROM asignacion_reportero ar WHERE ar.id_evento = e.id_evento) " +
			"GROUP BY e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
			"ORDER BY e.fecha_evento, e.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idAgencia);

		List<EventoDTO> events = new ArrayList<>();
		for (Object[] r : rows) {
			events.add(new EventoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3],
				(String) r[4]
			));
		}
		return events;
	}

	public List<EventoDTO> getEventosConAsignacion(int idAgencia) {
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento, " +
			"COALESCE(GROUP_CONCAT(t.nombre, ', '), '') AS tematicas " +
			"FROM evento e " +
			"LEFT JOIN evento_tematica et ON et.id_evento = e.id_evento " +
			"LEFT JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"WHERE e.id_agencia = ? " +
			"AND EXISTS (SELECT 1 FROM asignacion_reportero ar WHERE ar.id_evento = e.id_evento) " +
			"GROUP BY e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
			"ORDER BY e.fecha_evento, e.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idAgencia);

		List<EventoDTO> events = new ArrayList<>();
		for (Object[] r : rows) {
			events.add(new EventoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3],
				(String) r[4]
			));
		}
		return events;
	}

	public List<ReporteroDTO> getReporterosdeEvento(int idEvento) {
		String sql =
			"SELECT r.id_reportero, r.id_agencia, r.nombre, " +
			"COALESCE(GROUP_CONCAT(t.nombre, ', '), '') AS tematicas " +
			"FROM asignacion_reportero ar " +
			"JOIN reportero r ON r.id_reportero = ar.id_reportero " +
			"LEFT JOIN reportero_tematica rt ON rt.id_reportero = r.id_reportero " +
			"LEFT JOIN tematica t ON t.id_tematica = rt.id_tematica " +
			"WHERE ar.id_evento = ? " +
			"GROUP BY r.id_reportero, r.id_agencia, r.nombre " +
			"ORDER BY r.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		List<ReporteroDTO> reporteros = new ArrayList<>();

		for (Object[] r : rows) {
			reporteros.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3]
			));
		}
		return reporteros;
	}

	public List<ReporteroDTO> getReporterosDisponiblesParaEvento(int idEvento, boolean soloEspecialistas) {
		EventoDTO evento = getEventoById(idEvento);

		String sql =
			"SELECT r.id_reportero, r.id_agencia, r.nombre, " +
			"COALESCE(GROUP_CONCAT(DISTINCT t.nombre), '') AS tematicas " +
			"FROM reportero r " +
			"LEFT JOIN reportero_tematica rt ON rt.id_reportero = r.id_reportero " +
			"LEFT JOIN tematica t ON t.id_tematica = rt.id_tematica ";

		if (soloEspecialistas) {
			sql +=
				"WHERE r.id_agencia = ? " +
				"AND EXISTS ( " +
					"SELECT 1 " +
					"FROM reportero_tematica rt2 " +
					"JOIN evento_tematica et2 ON et2.id_tematica = rt2.id_tematica " +
					"WHERE rt2.id_reportero = r.id_reportero AND et2.id_evento = ? " +
				") ";
		} else {
			sql +=
				"WHERE r.id_agencia = ? ";
		}

		sql +=
			"AND NOT EXISTS ( " +
				"SELECT 1 FROM asignacion_reportero ar0 " +
				"WHERE ar0.id_evento = ? AND ar0.id_reportero = r.id_reportero " +
			") " +
			"AND NOT EXISTS ( " +
				"SELECT 1 " +
				"FROM asignacion_reportero ar " +
				"JOIN evento e2 ON e2.id_evento = ar.id_evento " +
				"WHERE ar.id_reportero = r.id_reportero " +
				"AND e2.fecha_evento = ? " +
				"AND ar.id_evento <> ? " +
			") " +
			"GROUP BY r.id_reportero, r.id_agencia, r.nombre " +
			"ORDER BY r.nombre";

		List<Object[]> rows;
		if (soloEspecialistas) {
			rows = db.executeQueryArray(sql,
				evento.getIdAgencia(),
				idEvento,
				idEvento,
				evento.getFechaEvento(),
				idEvento);
		} else {
			rows = db.executeQueryArray(sql,
				evento.getIdAgencia(),
				idEvento,
				evento.getFechaEvento(),
				idEvento);
		}

		List<ReporteroDTO> rep = new ArrayList<>();
		for (Object[] r : rows) {
			rep.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3]
			));
		}
		return rep;
	}

	private EventoDTO getEventoById(int idEvento) {
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento, " +
			"COALESCE(GROUP_CONCAT(t.nombre, ', '), '') AS tematicas " +
			"FROM evento e " +
			"LEFT JOIN evento_tematica et ON et.id_evento = e.id_evento " +
			"LEFT JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"WHERE e.id_evento = ? " +
			"GROUP BY e.id_evento, e.id_agencia, e.nombre, e.fecha_evento";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		if (rows.isEmpty()) {
			throw new ApplicationException("El evento seleccionado ya no existe.");
		}

		Object[] r = rows.get(0);
		return new EventoDTO(
			((Number) r[0]).intValue(),
			((Number) r[1]).intValue(),
			(String) r[2],
			(String) r[3],
			(String) r[4]
		);
	}

	public void asignarInicial(int idEvento, List<Integer> idsReporteros) {
		if (idsReporteros == null || idsReporteros.isEmpty()) {
			throw new ApplicationException("Debes asignar al menos un reportero.");
		}

		EventoDTO evento = getEventoById(idEvento);

		if (eventoTieneAsignaciones(idEvento)) {
			throw new ApplicationException("No se puede asignar: el evento ya tiene reporteros asignados.");
		}

		validarListaFinal(evento, idEvento, idsReporteros);

		String insert = "INSERT INTO asignacion_reportero(id_evento, id_reportero) VALUES (?, ?)";
		for (Integer idReportero : idsReporteros) {
			if (idReportero == null) continue;
			db.executeUpdate(insert, idEvento, idReportero);
		}
	}

	public void guardarAsignacionFinal(int idEvento, List<Integer> idsFinales) {
		EventoDTO evento = getEventoById(idEvento);
		if (idsFinales == null) idsFinales = new ArrayList<>();

		validarListaFinal(evento, idEvento, idsFinales);

		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", idEvento);

		String insert = "INSERT INTO asignacion_reportero(id_evento, id_reportero) VALUES (?, ?)";
		for (Integer idReportero : idsFinales) {
			if (idReportero == null) continue;
			db.executeUpdate(insert, idEvento, idReportero);
		}
	}

	private void validarListaFinal(EventoDTO evento, int idEvento, List<Integer> idsFinales) {
		Set<Integer> set = new HashSet<>();
		for (Integer id : idsFinales) {
			if (id == null) continue;
			if (!set.add(id)) {
				throw new ApplicationException("Hay reporteros duplicados en la asignación.");
			}
		}

		for (Integer idReportero : set) {
			if (!reporteroPerteneceAAgencia(idReportero, evento.getIdAgencia())) {
				throw new ApplicationException("Hay un reportero que no pertenece a la agencia del evento.");
			}
		}

		for (Integer idReportero : set) {
			if (!reporteroDisponibleEnFecha(idReportero, evento.getFechaEvento(), idEvento)) {
				throw new ApplicationException("Hay un reportero que ya está asignado a otro evento en esa fecha.");
			}
		}
	}

	private boolean eventoTieneAsignaciones(int idEvento) {
		String sql = "SELECT 1 FROM asignacion_reportero WHERE id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento).isEmpty();
	}

	private boolean reporteroPerteneceAAgencia(int idReportero, int idAgencia) {
		String sql = "SELECT 1 FROM reportero WHERE id_reportero = ? AND id_agencia = ? LIMIT 1";
		return !db.executeQueryArray(sql, idReportero, idAgencia).isEmpty();
	}

	private boolean reporteroDisponibleEnFecha(int idReportero, String fechaISO, int idEvento) {
		String sql =
			"SELECT 1 " +
			"FROM asignacion_reportero ar " +
			"JOIN evento e ON e.id_evento = ar.id_evento " +
			"WHERE ar.id_reportero = ? AND e.fecha_evento = ? AND ar.id_evento <> ? " +
			"LIMIT 1";
		return db.executeQueryArray(sql, idReportero, fechaISO, idEvento).isEmpty();
	}
}